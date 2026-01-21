package com.example.squarespool.service;

import com.example.squarespool.config.AppProperties;
import com.example.squarespool.dto.AdminBoardResponse;
import com.example.squarespool.dto.BetOptionResponse;
import com.example.squarespool.dto.BoardSnapshot;
import com.example.squarespool.dto.BoardSummaryResponse;
import com.example.squarespool.dto.CreateBoardRequest;
import com.example.squarespool.dto.GameOptionResponse;
import com.example.squarespool.dto.PurchaseRequest;
import com.example.squarespool.dto.SportOptionResponse;
import com.example.squarespool.dto.UpdateBoardRequest;
import com.example.squarespool.dto.SquareSnapshot;
import com.example.squarespool.model.Board;
import com.example.squarespool.model.BoardStatus;
import com.example.squarespool.model.Quarter;
import com.example.squarespool.model.Square;
import com.example.squarespool.model.SquareStatus;
import com.example.squarespool.repository.BoardRepository;
import com.example.squarespool.repository.SquareRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

@Service
public class BoardService {
    private final BoardRepository boardRepository;
    private final SquareRepository squareRepository;
    private final AppProperties appProperties;
    private final SimpMessagingTemplate messagingTemplate;

    public BoardService(BoardRepository boardRepository,
                        SquareRepository squareRepository,
                        AppProperties appProperties,
                        SimpMessagingTemplate messagingTemplate) {
        this.boardRepository = boardRepository;
        this.squareRepository = squareRepository;
        this.appProperties = appProperties;
        this.messagingTemplate = messagingTemplate;
    }

    @Transactional
    public Board createBoard(CreateBoardRequest request) {
        String sportType = normalizeSport(request.getSportType());
        String gameName = normalizeGameName(request.getGameName(), request.getHomeTeam(), request.getAwayTeam());
        int boardNumber = nextBoardNumber(sportType, gameName, request.getPriceCents());

        Board board = new Board();
        board.setName(request.getName());
        board.setHomeTeam(request.getHomeTeam());
        board.setAwayTeam(request.getAwayTeam());
        board.setSportType(sportType);
        board.setGameName(gameName);
        board.setBoardNumber(boardNumber);
        board.setPriceCents(request.getPriceCents());
        board.setHousePercent(request.getHousePercent());
        board.setMinSquaresToActivate(request.getMinSquaresToActivate());
        return saveBoardWithSquares(board);
    }

    @Transactional(readOnly = true)
    public List<Board> listBoards() {
        return boardRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<AdminBoardResponse> listBoardsForAdmin() {
        return boardRepository.findAll().stream()
                .sorted(Comparator.comparing(Board::getCreatedAt).reversed())
                .map(AdminBoardResponse::new)
                .toList();
    }

    @Transactional
    public Board updateBoard(Long boardId, UpdateBoardRequest request) {
        Board board = loadBoard(boardId);
        String normalizedSport = normalizeSport(request.getSportType());
        String normalizedGame = normalizeGameName(request.getGameName(), request.getHomeTeam(), request.getAwayTeam());
        String currentSport = normalizeSport(board.getSportType());
        String currentGame = normalizeGameName(board.getGameName(), board.getHomeTeam(), board.getAwayTeam());
        boolean keyChanged = !currentSport.equalsIgnoreCase(normalizedSport)
                || !currentGame.equalsIgnoreCase(normalizedGame)
                || board.getPriceCents() != request.getPriceCents();
        if (keyChanged) {
            board.setBoardNumber(nextBoardNumber(normalizedSport, normalizedGame, request.getPriceCents()));
        }
        board.setName(request.getName());
        board.setHomeTeam(request.getHomeTeam());
        board.setAwayTeam(request.getAwayTeam());
        board.setSportType(normalizedSport);
        board.setGameName(normalizedGame);
        board.setPriceCents(request.getPriceCents());
        board.setHousePercent(request.getHousePercent());
        board.setMinSquaresToActivate(request.getMinSquaresToActivate());
        Board saved = boardRepository.save(board);
        broadcastSnapshot(saved.getId());
        return saved;
    }

    @Transactional
    public void deleteBoard(Long boardId) {
        squareRepository.deleteByBoardId(boardId);
        boardRepository.deleteById(boardId);
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
        for (Square square : squares) {
            square.setStatus(SquareStatus.TAKEN);
            square.setOwnerName(request.getCustomerName());
            square.setReservedBySessionId(null);
            square.setReservedUntil(null);
        }
        squareRepository.saveAll(squares);

        if (board.getStatus() == BoardStatus.OPEN && isBoardFull(boardId)) {
            lockBoard(board);
            ensureFollowOnBoard(board);
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

    private void ensureFollowOnBoard(Board board) {
        if (board.getSportType() == null || board.getGameName() == null) {
            return;
        }
        int nextNumber = nextBoardNumber(board.getSportType(), board.getGameName(), board.getPriceCents());
        Board nextBoard = new Board();
        nextBoard.setName("Board #" + nextNumber);
        nextBoard.setHomeTeam(board.getHomeTeam());
        nextBoard.setAwayTeam(board.getAwayTeam());
        nextBoard.setSportType(board.getSportType());
        nextBoard.setGameName(board.getGameName());
        nextBoard.setBoardNumber(nextNumber);
        nextBoard.setPriceCents(board.getPriceCents());
        nextBoard.setHousePercent(board.getHousePercent());
        nextBoard.setMinSquaresToActivate(board.getMinSquaresToActivate());
        saveBoardWithSquares(nextBoard);
    }

    private Board saveBoardWithSquares(Board board) {
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

    public List<SportOptionResponse> listSports() {
        Set<String> sports = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        for (Board board : boardRepository.findAll()) {
            sports.add(normalizeSport(board.getSportType()));
        }
        return sports.stream().map(SportOptionResponse::new).toList();
    }

    public List<BetOptionResponse> listBetOptions(String sportType, String gameName) {
        String normalizedSport = sportType == null ? null : normalizeSport(sportType);
        String normalizedGame = gameName == null ? null : normalizeGameName(gameName, null, null);
        return boardRepository.findAll().stream()
                .filter(board -> normalizedSport == null
                        || normalizeSport(board.getSportType()).equalsIgnoreCase(normalizedSport))
                .filter(board -> normalizedGame == null
                        || normalizeGameName(board.getGameName(), board.getHomeTeam(), board.getAwayTeam())
                        .equalsIgnoreCase(normalizedGame))
                .map(Board::getPriceCents)
                .distinct()
                .sorted()
                .map(priceCents -> new BetOptionResponse(priceCents, formatCurrencyLabel(priceCents)))
                .toList();
    }

    public List<GameOptionResponse> listGames(String sportType) {
        String normalizedSport = normalizeSport(sportType);
        Map<String, GameOptionResponse> games = new LinkedHashMap<>();
        for (Board board : boardRepository.findAll()) {
            String boardSport = normalizeSport(board.getSportType());
            if (!boardSport.equalsIgnoreCase(normalizedSport)) {
                continue;
            }
            String gameName = normalizeGameName(board.getGameName(), board.getHomeTeam(), board.getAwayTeam());
            games.putIfAbsent(gameName, new GameOptionResponse(gameName, board.getHomeTeam(), board.getAwayTeam()));
        }
        return new ArrayList<>(games.values());
    }

    public List<BoardSummaryResponse> listBoards(String sportType, String gameName, int priceCents) {
        String normalizedSport = normalizeSport(sportType);
        String normalizedGame = normalizeGameName(gameName, null, null);
        List<Board> boards = boardRepository.findAll().stream()
                .filter(board -> normalizeSport(board.getSportType()).equalsIgnoreCase(normalizedSport))
                .filter(board -> normalizeGameName(board.getGameName(), board.getHomeTeam(), board.getAwayTeam())
                        .equalsIgnoreCase(normalizedGame))
                .filter(board -> board.getPriceCents() == priceCents)
                .sorted((a, b) -> Integer.compare(a.getBoardNumber(), b.getBoardNumber()))
                .toList();
        List<BoardSummaryResponse> summaries = new ArrayList<>();
        for (Board board : boards) {
            int openSquares = (int) squareRepository.countByBoardIdAndStatus(board.getId(), SquareStatus.EMPTY);
            boolean full = openSquares == 0;
            String label = board.getBoardNumber() > 0 ? "Board #" + board.getBoardNumber() : board.getName();
            summaries.add(new BoardSummaryResponse(board.getId(), label, openSquares, 100, full, board.getStatus()));
        }
        return summaries;
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

    private String formatCurrencyLabel(int amountCents) {
        return String.format("$%.2f", amountCents / 100.0);
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
        snapshot.setStatus(board.getStatus());
        snapshot.setHomeScore(board.getHomeScore());
        snapshot.setAwayScore(board.getAwayScore());
        snapshot.setCurrentQuarter(board.getCurrentQuarter());
        snapshot.setConfirmedQuarters(board.getConfirmedQuarters());
        int purchasedCount = (int) squares.stream().filter(square -> square.getStatus() == SquareStatus.TAKEN).count();
        snapshot.setPurchasedCount(purchasedCount);
        snapshot.setActive(purchasedCount >= board.getMinSquaresToActivate());
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

    private int nextBoardNumber(String sportType, String gameName, int priceCents) {
        return boardRepository.findMaxBoardNumber(sportType, gameName, priceCents) + 1;
    }

    private String normalizeSport(String sportType) {
        if (sportType == null || sportType.isBlank()) {
            return "General";
        }
        return sportType.trim();
    }

    private String normalizeGameName(String gameName, String homeTeam, String awayTeam) {
        if (gameName != null && !gameName.isBlank()) {
            return gameName.trim();
        }
        if (homeTeam != null && awayTeam != null) {
            return homeTeam.trim() + " vs " + awayTeam.trim();
        }
        return "Matchup";
    }
}
