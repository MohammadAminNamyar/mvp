package com.example.squarespool.service;

import com.example.squarespool.config.AppProperties;
import com.example.squarespool.dto.BoardSnapshot;
import com.example.squarespool.dto.CreateBoardRequest;
import com.example.squarespool.dto.PurchaseRequest;
import com.example.squarespool.dto.SquareSnapshot;
import com.example.squarespool.config.TpiProperties;
import com.example.squarespool.model.Board;
import com.example.squarespool.model.BoardStatus;
import com.example.squarespool.model.Quarter;
import com.example.squarespool.model.Square;
import com.example.squarespool.model.SquareStatus;
import com.example.squarespool.repository.BoardRepository;
import com.example.squarespool.repository.SquareRepository;
import com.example.squarespool.tpi.TpiClient;
import com.example.squarespool.tpi.TpiCustomer;
import com.example.squarespool.tpi.dto.DebitRequest;
import com.example.squarespool.tpi.dto.DebitResponse;
import com.example.squarespool.tpi.dto.MoneyAmount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class BoardService {
    private static final Logger log = LoggerFactory.getLogger(BoardService.class);
    private final BoardRepository boardRepository;
    private final SquareRepository squareRepository;
    private final AppProperties appProperties;
    private final TpiProperties tpiProperties;
    private final TpiClient tpiClient;
    private final SimpMessagingTemplate messagingTemplate;

    public BoardService(BoardRepository boardRepository,
                        SquareRepository squareRepository,
                        AppProperties appProperties,
                        TpiProperties tpiProperties,
                        TpiClient tpiClient,
                        SimpMessagingTemplate messagingTemplate) {
        this.boardRepository = boardRepository;
        this.squareRepository = squareRepository;
        this.appProperties = appProperties;
        this.tpiProperties = tpiProperties;
        this.tpiClient = tpiClient;
        this.messagingTemplate = messagingTemplate;
    }

    @Transactional
    public Board createBoard(CreateBoardRequest request) {
        int payoutSum = request.getPayoutQ1Percent()
                + request.getPayoutQ2Percent()
                + request.getPayoutQ3Percent()
                + request.getPayoutQ4Percent();
        if (payoutSum != 100) {
            throw new IllegalStateException("Payout percentages must total 100");
        }
        Board board = new Board();
        board.setName(request.getName());
        board.setHomeTeam(request.getHomeTeam());
        board.setAwayTeam(request.getAwayTeam());
        board.setPriceCents(request.getPriceCents());
        board.setHousePercent(request.getHousePercent());
        board.setMinSquaresToActivate(request.getMinSquaresToActivate());
        board.setPayoutQ1Percent(request.getPayoutQ1Percent());
        board.setPayoutQ2Percent(request.getPayoutQ2Percent());
        board.setPayoutQ3Percent(request.getPayoutQ3Percent());
        board.setPayoutQ4Percent(request.getPayoutQ4Percent());
        Board saved = boardRepository.save(board);

        List<Square> squares = new ArrayList<>();
        for (int row = 0; row < 10; row++) {
            for (int col = 0; col < 10; col++) {
                Square square = new Square();
                square.setBoard(saved);
                square.setRowIndex(row);
                square.setColIndex(col);
                square.setIdx(row * 10 + col);
                square.setStatus(SquareStatus.EMPTY);
                squares.add(square);
            }
        }
        squareRepository.saveAll(squares);
        broadcastSnapshot(saved.getId());
        return saved;
    }

    @Transactional(readOnly = true)
    public List<Board> listBoards() {
        return boardRepository.findAll();
    }

    @Transactional(readOnly = true)
    public BoardSnapshot getSnapshot(Long boardId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("Board not found"));
        List<Square> squares = squareRepository.findByBoardId(boardId);
        return toSnapshot(board, squares);
    }

    @Transactional
    public void reserveSquare(Long boardId, int idx, String sessionId) {
        Board board = loadBoard(boardId);
        if (board.getStatus() != BoardStatus.OPEN) {
            throw new IllegalStateException("Board is locked");
        }
        Square square = loadSquare(boardId, idx);
        if (square.getStatus() != SquareStatus.EMPTY) {
            throw new IllegalStateException("Square not available");
        }
        square.setStatus(SquareStatus.RESERVED);
        square.setReservedBySessionId(sessionId);
        square.setReservedUntil(Instant.now().plusSeconds(appProperties.getReservationTtlSeconds()));
        squareRepository.save(square);
        broadcastSnapshot(boardId);
    }

    @Transactional
    public void unreserveSquare(Long boardId, int idx, String sessionId) {
        Square square = loadSquare(boardId, idx);
        if (square.getStatus() == SquareStatus.RESERVED && Objects.equals(square.getReservedBySessionId(), sessionId)) {
            square.setStatus(SquareStatus.EMPTY);
            square.setReservedBySessionId(null);
            square.setReservedUntil(null);
            squareRepository.save(square);
            broadcastSnapshot(boardId);
        }
    }

    @Transactional
    public void purchase(Long boardId, PurchaseRequest request) {
        Board board = loadBoard(boardId);
        if (EnumSet.of(BoardStatus.STARTED, BoardStatus.FINISHED).contains(board.getStatus())) {
            throw new IllegalStateException("Board is locked");
        }
        TpiCustomer customer = tpiClient.resolveCustomer(request.getServiceTicket(), request.getCustomerName());
        Instant now = Instant.now();
        List<Square> squares = request.getIndices().stream()
                .map(idx -> loadSquare(boardId, idx))
                .toList();
        for (Square square : squares) {
            if (square.getStatus() != SquareStatus.RESERVED) {
                throw new IllegalStateException("Square not reserved");
            }
            if (!Objects.equals(square.getReservedBySessionId(), request.getSessionId())) {
                throw new IllegalStateException("Square reserved by another session");
            }
            if (square.getReservedUntil() != null && square.getReservedUntil().isBefore(now)) {
                throw new IllegalStateException("Reservation expired");
            }
        }
        long totalCents = board.getPriceCents() * (long) squares.size();
        log.info("TPI debit start board={} session={} squares={} amountCents={}", boardId, request.getSessionId(), request.getIndices(), totalCents);
        DebitResponse debitResponse = tpiClient.debit(buildDebitRequest(board, request, totalCents));
        log.info("TPI debit response board={} session={} code={} balance={}", boardId, request.getSessionId(), debitResponse.getResponseCode(), debitResponse.getAleaAccountBalance() != null ? debitResponse.getAleaAccountBalance().getValue() : null);
        if (debitResponse.getResponseCode() != null && debitResponse.getResponseCode() != 0) {
            String message = debitResponse.getResponseMessage() != null
                    ? debitResponse.getResponseMessage()
                    : "Payment declined";
            log.warn("TPI debit declined board={} session={} code={} message={}", boardId, request.getSessionId(), debitResponse.getResponseCode(), message);
            throw new IllegalStateException(message);
        }
        for (Square square : squares) {
            square.setStatus(SquareStatus.TAKEN);
            square.setOwnerName(customer.getDisplayName());
            square.setReservedBySessionId(null);
            square.setReservedUntil(null);
        }
        squareRepository.saveAll(squares);

        if (board.getStatus() == BoardStatus.OPEN && isBoardFull(boardId)) {
            lockBoard(board);
        }
        broadcastSnapshot(boardId);
    }

    @Transactional
    public void startGame(Long boardId) {
        Board board = loadBoard(boardId);
        long purchasedCount = squareRepository.countByBoardIdAndStatus(boardId, SquareStatus.TAKEN);
        if (purchasedCount < board.getMinSquaresToActivate()) {
            throw new IllegalStateException("Not enough squares to activate");
        }
        board.setStatus(BoardStatus.STARTED);
        startGameClock(board);
        if (appProperties.isHouseOnLock()) {
            List<Square> squares = squareRepository.findByBoardId(boardId);
            for (Square square : squares) {
                if (square.getStatus() == SquareStatus.EMPTY || square.getStatus() == SquareStatus.RESERVED) {
                    square.setStatus(SquareStatus.HOUSE);
                    square.setOwnerName("HOUSE");
                    square.setReservedBySessionId(null);
                    square.setReservedUntil(null);
                }
            }
            squareRepository.saveAll(squares);
        }
        ensureDigits(board);
        boardRepository.save(board);
        broadcastSnapshot(boardId);
    }

    @Transactional
    public void updateScore(Long boardId, int homeScore, int awayScore) {
        Board board = loadBoard(boardId);
        board.setHomeScore(homeScore);
        board.setAwayScore(awayScore);
        boardRepository.save(board);
        broadcastSnapshot(boardId);
    }

    @Transactional
    public void updateScore(Long boardId, int homeScore, int awayScore, String gameClock) {
        Board board = loadBoard(boardId);
        board.setHomeScore(homeScore);
        board.setAwayScore(awayScore);
        applyGameClock(board, gameClock);
        boardRepository.save(board);
        broadcastSnapshot(boardId);
    }

    @Transactional
    public void confirmQuarter(Long boardId, Quarter quarter) {
        Board board = loadBoard(boardId);
        ensureDigits(board);
        int winnerIdx = computeWinnerIdx(board);
        Square winner = loadSquare(boardId, winnerIdx);
        switch (quarter) {
            case Q1 -> winner.setWonQ1(true);
            case Q2 -> winner.setWonQ2(true);
            case Q3 -> winner.setWonQ3(true);
            case Q4 -> winner.setWonFinal(true);
        }
        board.getConfirmedQuarters().add(quarter);
        board.setCurrentQuarter(quarter);
        if (quarter == Quarter.Q4) {
            board.setStatus(BoardStatus.FINISHED);
        }
        squareRepository.save(winner);
        boardRepository.save(board);
        broadcastSnapshot(boardId);
    }

    @Transactional
    public void resetBoard(Long boardId) {
        Board board = loadBoard(boardId);
        board.setStatus(BoardStatus.OPEN);
        board.setRowDigits(null);
        board.setColDigits(null);
        board.setHomeScore(0);
        board.setAwayScore(0);
        board.setGameClock(null);
        board.setGameClockSeconds(0);
        board.setGameClockRunning(false);
        board.setGameClockUpdatedAt(null);
        board.setCurrentQuarter(Quarter.Q1);
        board.getConfirmedQuarters().clear();
        boardRepository.save(board);

        List<Square> squares = squareRepository.findByBoardId(boardId);
        for (Square square : squares) {
            square.setStatus(SquareStatus.EMPTY);
            square.setOwnerName(null);
            square.setReservedBySessionId(null);
            square.setReservedUntil(null);
            square.setWonQ1(false);
            square.setWonQ2(false);
            square.setWonQ3(false);
            square.setWonFinal(false);
        }
        squareRepository.saveAll(squares);
        broadcastSnapshot(boardId);
    }

    @Transactional
    public Map<Long, Integer> releaseExpiredReservations() {
        Instant now = Instant.now();
        List<Square> expired = squareRepository.findExpiredReservations(now);
        if (expired.isEmpty()) {
            return Map.of();
        }
        Map<Long, Integer> touchedBoards = new HashMap<>();
        for (Square square : expired) {
            square.setStatus(SquareStatus.EMPTY);
            square.setReservedBySessionId(null);
            square.setReservedUntil(null);
            touchedBoards.merge(square.getBoard().getId(), 1, Integer::sum);
        }
        squareRepository.saveAll(expired);
        return touchedBoards;
    }

    public List<Integer> generateDigits() {
        List<Integer> digits = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            digits.add(i);
        }
        Collections.shuffle(digits);
        return digits;
    }

    public int computeWinnerIdx(Board board) {
        List<Integer> rowDigits = parseDigits(board.getRowDigits());
        List<Integer> colDigits = parseDigits(board.getColDigits());
        int rowDigit = lastDigit(board.getHomeScore());
        int colDigit = lastDigit(board.getAwayScore());
        int rowIndex = rowDigits.indexOf(rowDigit);
        int colIndex = colDigits.indexOf(colDigit);
        if (rowIndex < 0 || colIndex < 0) {
            throw new IllegalStateException("Digits not assigned");
        }
        return rowIndex * 10 + colIndex;
    }

    private int lastDigit(int score) {
        return Math.abs(score) % 10;
    }

    private void lockBoard(Board board) {
        board.setStatus(BoardStatus.LOCKED);
        ensureDigits(board);
        boardRepository.save(board);
    }

    private void ensureDigits(Board board) {
        if (board.getRowDigits() == null || board.getColDigits() == null) {
            board.setRowDigits(joinDigits(generateDigits()));
            board.setColDigits(joinDigits(generateDigits()));
        }
    }

    private boolean isBoardFull(Long boardId) {
        List<Square> squares = squareRepository.findByBoardId(boardId);
        return squares.stream().noneMatch(square -> square.getStatus() == SquareStatus.EMPTY || square.getStatus() == SquareStatus.RESERVED);
    }

    private Board loadBoard(Long boardId) {
        return boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("Board not found"));
    }

    private Square loadSquare(Long boardId, int idx) {
        return squareRepository.findByBoardIdAndIdx(boardId, idx)
                .orElseThrow(() -> new IllegalArgumentException("Square not found"));
    }

    private String joinDigits(List<Integer> digits) {
        return digits.stream().map(String::valueOf).collect(Collectors.joining(","));
    }

    private List<Integer> parseDigits(String digits) {
        if (digits == null || digits.isBlank()) {
            return List.of();
        }
        String[] parts = digits.split(",");
        List<Integer> result = new ArrayList<>();
        for (String part : parts) {
            result.add(Integer.parseInt(part));
        }
        return result;
    }

    private BoardSnapshot toSnapshot(Board board, List<Square> squares) {
        BoardSnapshot snapshot = new BoardSnapshot();
        snapshot.setBoardId(board.getId());
        snapshot.setName(board.getName());
        snapshot.setHomeTeam(board.getHomeTeam());
        snapshot.setAwayTeam(board.getAwayTeam());
        snapshot.setPriceCents(board.getPriceCents());
        snapshot.setHousePercent(board.getHousePercent());
        snapshot.setMinSquaresToActivate(board.getMinSquaresToActivate());
        snapshot.setPayoutQ1Percent(board.getPayoutQ1Percent());
        snapshot.setPayoutQ2Percent(board.getPayoutQ2Percent());
        snapshot.setPayoutQ3Percent(board.getPayoutQ3Percent());
        snapshot.setPayoutQ4Percent(board.getPayoutQ4Percent());
        snapshot.setStatus(board.getStatus());
        snapshot.setHomeScore(board.getHomeScore());
        snapshot.setAwayScore(board.getAwayScore());
        snapshot.setGameClock(resolveGameClock(board));
        snapshot.setCurrentQuarter(board.getCurrentQuarter());
        snapshot.setConfirmedQuarters(board.getConfirmedQuarters());
        int purchasedCount = (int) squares.stream().filter(square -> square.getStatus() == SquareStatus.TAKEN).count();
        snapshot.setPurchasedCount(purchasedCount);
        snapshot.setActive(purchasedCount >= board.getMinSquaresToActivate());
        applyPrizeBreakdown(snapshot, board, purchasedCount);
        boolean revealDigits = board.getStatus() != BoardStatus.OPEN;
        snapshot.setDigitsRevealed(revealDigits);
        if (revealDigits) {
            snapshot.setRowDigits(parseDigits(board.getRowDigits()));
            snapshot.setColDigits(parseDigits(board.getColDigits()));
        } else {
            snapshot.setRowDigits(List.of());
            snapshot.setColDigits(List.of());
        }
        if (board.getRowDigits() != null && board.getColDigits() != null) {
            snapshot.setCurrentWinnerIdx(computeWinnerIdx(board));
        }
        snapshot.setSquares(squares.stream().map(this::toSquareSnapshot).toList());
        return snapshot;
    }

    public boolean tickGameClock(Board board, Instant now) {
        if (!board.isGameClockRunning()) {
            return false;
        }
        Instant lastUpdate = board.getGameClockUpdatedAt();
        if (lastUpdate == null) {
            board.setGameClockUpdatedAt(now);
            return false;
        }
        long deltaSeconds = Math.max(0, java.time.Duration.between(lastUpdate, now).getSeconds());
        if (deltaSeconds == 0) {
            return false;
        }
        board.setGameClockSeconds(board.getGameClockSeconds() + (int) deltaSeconds);
        board.setGameClockUpdatedAt(now);
        board.setGameClock(formatClock(board.getGameClockSeconds()));
        return true;
    }

    private void applyPrizeBreakdown(BoardSnapshot snapshot, Board board, int purchasedCount) {
        long totalCents = (long) purchasedCount * board.getPriceCents();
        long houseCut = Math.round(totalCents * (board.getHousePercent() / 100.0));
        long pool = Math.max(0L, totalCents - houseCut);
        int q1 = percentOf(pool, board.getPayoutQ1Percent());
        int q2 = percentOf(pool, board.getPayoutQ2Percent());
        int q3 = percentOf(pool, board.getPayoutQ3Percent());
        int q4 = percentOf(pool, board.getPayoutQ4Percent());
        int sum = q1 + q2 + q3 + q4;
        int remainder = (int) Math.max(0L, pool - sum);
        q4 += remainder;
        snapshot.setPrizePoolCents((int) pool);
        snapshot.setPrizeQ1Cents(q1);
        snapshot.setPrizeQ2Cents(q2);
        snapshot.setPrizeQ3Cents(q3);
        snapshot.setPrizeQ4Cents(q4);
    }

    private int percentOf(long total, int percent) {
        if (total <= 0 || percent <= 0) {
            return 0;
        }
        return (int) Math.round(total * (percent / 100.0));
    }

    private void startGameClock(Board board) {
        board.setGameClockSeconds(0);
        board.setGameClock("00:00");
        board.setGameClockRunning(true);
        board.setGameClockUpdatedAt(Instant.now());
    }

    private void applyGameClock(Board board, String gameClock) {
        String value = gameClock == null ? "" : gameClock.trim();
        if (!value.isEmpty()) {
            board.setGameClock(value);
            board.setGameClockSeconds(parseClockSeconds(value));
            board.setGameClockUpdatedAt(Instant.now());
            board.setGameClockRunning(true);
            return;
        }
        if (board.getGameClockUpdatedAt() == null) {
            board.setGameClockUpdatedAt(Instant.now());
        }
        board.setGameClockRunning(true);
    }

    private String resolveGameClock(Board board) {
        if (board.isGameClockRunning() && board.getGameClockUpdatedAt() != null) {
            Instant now = Instant.now();
            long deltaSeconds = Math.max(0, java.time.Duration.between(board.getGameClockUpdatedAt(), now).getSeconds());
            int computed = board.getGameClockSeconds() + (int) deltaSeconds;
            return formatClock(computed);
        }
        if (board.getGameClock() != null && !board.getGameClock().isBlank()) {
            return board.getGameClock();
        }
        return formatClock(board.getGameClockSeconds());
    }

    private int parseClockSeconds(String clock) {
        String value = clock.trim();
        if (value.endsWith("'")) {
            value = value.substring(0, value.length() - 1);
        }
        String[] parts = value.split(":");
        if (parts.length != 2) {
            return 0;
        }
        try {
            int minutes = Integer.parseInt(parts[0]);
            int seconds = Integer.parseInt(parts[1]);
            return Math.max(0, minutes * 60 + seconds);
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    private String formatClock(int totalSeconds) {
        int minutes = Math.max(0, totalSeconds) / 60;
        int seconds = Math.max(0, totalSeconds) % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    private SquareSnapshot toSquareSnapshot(Square square) {
        SquareSnapshot snapshot = new SquareSnapshot();
        snapshot.setIdx(square.getIdx());
        snapshot.setRowIndex(square.getRowIndex());
        snapshot.setColIndex(square.getColIndex());
        snapshot.setStatus(square.getStatus());
        if (appProperties.isShowPurchaserNames()) {
            snapshot.setOwnerName(square.getOwnerName());
        } else {
            snapshot.setOwnerName(null);
        }
        snapshot.setReservedBySessionId(square.getReservedBySessionId());
        snapshot.setReservedUntil(square.getReservedUntil());
        snapshot.setWonQ1(square.isWonQ1());
        snapshot.setWonQ2(square.isWonQ2());
        snapshot.setWonQ3(square.isWonQ3());
        snapshot.setWonFinal(square.isWonFinal());
        return snapshot;
    }

    public void broadcastSnapshot(Long boardId) {
        BoardSnapshot snapshot = getSnapshot(boardId);
        messagingTemplate.convertAndSend("/topic/boards/" + boardId + "/snapshot", snapshot);
    }

    private DebitRequest buildDebitRequest(Board board, PurchaseRequest request, long amountCents) {
        DebitRequest debitRequest = new DebitRequest();
        debitRequest.setThirdPartyTransactionTypeId(tpiProperties.getThirdPartyTransactionTypeId());
        debitRequest.setThirdPartyTransactionId(UUID.randomUUID().toString());
        debitRequest.setThirdPartyRoundId(String.valueOf(board.getId()));
        debitRequest.setCustomerId(request.getSessionId());
        debitRequest.setGameTypeId(tpiProperties.getGameTypeId());
        debitRequest.setGameTypeVariationId(tpiProperties.getGameTypeVariationId());
        debitRequest.setAmount(new MoneyAmount(tpiProperties.getCurrency(), amountCents));
        debitRequest.setRoundToBeClosed(tpiProperties.isRoundToBeClosed());
        return debitRequest;
    }
}
