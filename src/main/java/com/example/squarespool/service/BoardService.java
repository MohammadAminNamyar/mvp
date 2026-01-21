package com.example.squarespool.service;

import com.example.squarespool.config.AppProperties;
import com.example.squarespool.config.TpiProperties;
import com.example.squarespool.dto.AdminBoardSummary;
import com.example.squarespool.dto.BetOption;
import com.example.squarespool.dto.BoardSnapshot;
import com.example.squarespool.dto.CreateBoardRequest;
import com.example.squarespool.dto.LobbyBoardResponse;
import com.example.squarespool.dto.LobbyGameResponse;
import com.example.squarespool.dto.PlayerHistoryBoard;
import com.example.squarespool.dto.PlayerHistorySquare;
import com.example.squarespool.dto.PurchaseRequest;
import com.example.squarespool.dto.SquareSnapshot;
import com.example.squarespool.dto.UpdateBoardRequest;
import com.example.squarespool.model.Board;
import com.example.squarespool.model.BoardStatus;
import com.example.squarespool.model.PayoutEvent;
import com.example.squarespool.model.PayoutEventType;
import com.example.squarespool.model.Quarter;
import com.example.squarespool.model.Square;
import com.example.squarespool.model.SquareStatus;
import com.example.squarespool.repository.BoardRepository;
import com.example.squarespool.repository.PayoutEventRepository;
import com.example.squarespool.repository.SquareRepository;
import com.example.squarespool.tpi.TpiClient;
import com.example.squarespool.tpi.TpiCustomer;
import com.example.squarespool.tpi.dto.DebitRequest;
import com.example.squarespool.tpi.dto.DebitResponse;
import com.example.squarespool.tpi.dto.MoneyAmount;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Comparator;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BoardService {
    private static final Logger log = LoggerFactory.getLogger(BoardService.class);
    private static final int TOTAL_SQUARES = 100;
    private static final int FINAL_EMPTY_RAKE_PERCENT = 10;
    private final BoardRepository boardRepository;
    private final SquareRepository squareRepository;
    private final AppProperties appProperties;
    private final TpiProperties tpiProperties;
    private final TpiClient tpiClient;
    private final SimpMessagingTemplate messagingTemplate;
    private final PayoutEventRepository payoutEventRepository;

    public BoardService(BoardRepository boardRepository,
                        SquareRepository squareRepository,
                        AppProperties appProperties,
                        TpiProperties tpiProperties,
                        TpiClient tpiClient,
                        SimpMessagingTemplate messagingTemplate,
                        PayoutEventRepository payoutEventRepository) {
        this.boardRepository = boardRepository;
        this.squareRepository = squareRepository;
        this.appProperties = appProperties;
        this.tpiProperties = tpiProperties;
        this.tpiClient = tpiClient;
        this.messagingTemplate = messagingTemplate;
        this.payoutEventRepository = payoutEventRepository;
    }

  @Transactional
  public Board createBoard(CreateBoardRequest request) {
    double payoutSum =
        request.getPayoutQ1Percent()
            + request.getPayoutQ2Percent()
            + request.getPayoutQ3Percent()
            + request.getPayoutQ4Percent();
    if (Math.abs(payoutSum - 100.0) > 0.01) {
      throw new IllegalStateException("Payout percentages must total 100");
    }
    Board board = new Board();
    board.setName(request.getName());
    board.setSportType(request.getSportType());
    board.setGameName(request.getGameName());
    String gameId =
        request.getGameId() != null && !request.getGameId().isBlank()
            ? request.getGameId()
            : UUID.randomUUID().toString();
    board.setGameId(gameId);
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

    initializeSquares(saved);
    broadcastSnapshot(saved.getId());
    return saved;
  }

  @Transactional(readOnly = true)
  public List<Board> listBoards() {
    return boardRepository.findAll();
  }

  @Transactional(readOnly = true)
  public List<String> listSports() {
    return boardRepository.findDistinctSportTypes();
  }

  @Transactional(readOnly = true)
  public List<LobbyGameResponse> listGamesBySport(String sportType) {
    List<Board> boards = boardRepository.findBySportTypeIgnoreCase(sportType);
    Map<String, Board> games = new LinkedHashMap<>();
    for (Board board : boards) {
      if (board.getGameId() == null || board.getGameId().isBlank()) {
        continue;
      }
      games.putIfAbsent(board.getGameId(), board);
    }
    return games.values().stream().map(this::toLobbyGame).toList();
  }

  @Transactional(readOnly = true)
  public List<LobbyBoardResponse> listBoardsByGameAndBet(String gameId, int priceCents) {
    List<Board> boards =
        boardRepository.findByGameIdAndPriceCentsOrderByCreatedAtAsc(gameId, priceCents);
    return boards.stream().map(this::toLobbyBoard).toList();
  }

  @Transactional(readOnly = true)
  public List<BetOption> listBetOptions() {
    return boardRepository.findDistinctPriceCentsOrderByPriceCentsAsc().stream()
        .map(amount -> new BetOption(amount, String.format("$%d", amount / 100)))
        .toList();
  }

  @Transactional(readOnly = true)
  public List<BetOption> listBetOptionsByGame(String gameId) {
    return boardRepository.findDistinctPriceCentsByGameIdOrderByPriceCentsAsc(gameId).stream()
        .map(amount -> new BetOption(amount, String.format("$%d", amount / 100)))
        .toList();
  }

  @Transactional(readOnly = true)
  public List<AdminBoardSummary> listAdminBoards() {
    return boardRepository.findAll().stream().map(this::toAdminSummary).toList();
  }

  @Transactional(readOnly = true)
  public List<PlayerHistoryBoard> getPlayerHistory(String sessionId) {
    if (sessionId == null || sessionId.isBlank()) {
      return List.of();
    }
    List<Square> ownedSquares = squareRepository.findByOwnerSessionIdWithFinishedBoard(sessionId);
    if (ownedSquares.isEmpty()) {
      return List.of();
    }
    Map<Long, List<Square>> squaresByBoard =
        ownedSquares.stream().collect(Collectors.groupingBy(square -> square.getBoard().getId()));
    List<PlayerHistoryBoard> results = new ArrayList<>();
    for (Map.Entry<Long, List<Square>> entry : squaresByBoard.entrySet()) {
      List<Square> userSquares = entry.getValue();
      Board board = userSquares.get(0).getBoard();
      List<Square> boardSquares = squareRepository.findByBoardId(board.getId());
      int purchasedCount =
          (int) boardSquares.stream().filter(square -> square.getStatus() == SquareStatus.TAKEN).count();
      PrizeBreakdown breakdown = computePrizeBreakdown(board, boardSquares, purchasedCount);
      int ownedCount =
          (int) userSquares.stream().filter(square -> square.getStatus() == SquareStatus.TAKEN).count();
      int totalSpent = ownedCount * board.getPriceCents();
      int totalWinnings =
          calculateWinnings(sessionId, ownedCount, boardSquares, breakdown);

      PlayerHistoryBoard response = new PlayerHistoryBoard();
      response.setBoardId(board.getId());
      response.setName(board.getName());
      response.setSportType(board.getSportType());
      response.setGameName(board.getGameName());
      response.setHomeTeam(board.getHomeTeam());
      response.setAwayTeam(board.getAwayTeam());
      response.setPriceCents(board.getPriceCents());
      response.setTotalSpentCents(totalSpent);
      response.setTotalWinningsCents(totalWinnings);
      response.setNetCents(totalWinnings - totalSpent);
      response.setFinalPrizeRefunded(breakdown.finalPrizeRefunded());
      response.setFinalRefundPerPlayerCents(breakdown.finalRefundPerPlayerCents());
      response.setSquares(
          userSquares.stream()
              .sorted((a, b) -> Integer.compare(a.getIdx(), b.getIdx()))
              .map(this::toPlayerHistorySquare)
              .toList());
      results.add(response);
    }
    results.sort((a, b) -> Long.compare(b.getBoardId(), a.getBoardId()));
    return results;
  }

  @Transactional
  public AdminBoardSummary updateBoard(Long boardId, UpdateBoardRequest request) {
    double payoutSum =
        request.getPayoutQ1Percent()
            + request.getPayoutQ2Percent()
            + request.getPayoutQ3Percent()
            + request.getPayoutQ4Percent();
    if (Math.abs(payoutSum - 100.0) > 0.01) {
      throw new IllegalStateException("Payout percentages must total 100");
    }
    Board board = loadBoard(boardId);
    board.setName(request.getName());
    board.setSportType(request.getSportType());
    board.setGameName(request.getGameName());
    if (request.getGameId() != null && !request.getGameId().isBlank()) {
      board.setGameId(request.getGameId());
    }
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
    return toAdminSummary(saved);
  }

  @Transactional
  public void deleteBoard(Long boardId) {
    squareRepository.deleteByBoardId(boardId);
    boardRepository.deleteById(boardId);
  }

  @Transactional(readOnly = true)
  public BoardSnapshot getSnapshot(Long boardId) {
    Board board =
        boardRepository
            .findById(boardId)
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
    if (square.getStatus() == SquareStatus.RESERVED
        && Objects.equals(square.getReservedBySessionId(), sessionId)) {
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
    boolean wasOpen = board.getStatus() == BoardStatus.OPEN;
    if (request.getCustomerName() == null || request.getCustomerName().isBlank()) {
      String suffix = request.getSessionId() != null && request.getSessionId().length() >= 6
          ? request.getSessionId().substring(0, 6)
          : String.valueOf(request.getSessionId());
      request.setCustomerName("Guest-" + suffix);
    }
    TpiCustomer customer =
        tpiClient.resolveCustomer(request.getServiceTicket(), request.getCustomerName());
    Instant now = Instant.now();
    List<Square> squares =
        request.getIndices().stream().map(idx -> loadSquare(boardId, idx)).toList();
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
    log.info(
        "TPI debit start board={} session={} squares={} amountCents={}",
        boardId,
        request.getSessionId(),
        request.getIndices(),
        totalCents);
    DebitResponse debitResponse = tpiClient.debit(buildDebitRequest(board, request, totalCents));
    log.info(
        "TPI debit response board={} session={} code={} balance={}",
        boardId,
        request.getSessionId(),
        debitResponse.getResponseCode(),
        debitResponse.getAleaAccountBalance() != null
            ? debitResponse.getAleaAccountBalance().getValue()
            : null);
    if (debitResponse.getResponseCode() != null && debitResponse.getResponseCode() != 0) {
      String message =
          debitResponse.getResponseMessage() != null
              ? debitResponse.getResponseMessage()
              : "Payment declined";
      log.warn(
          "TPI debit declined board={} session={} code={} message={}",
          boardId,
          request.getSessionId(),
          debitResponse.getResponseCode(),
          message);
      throw new IllegalStateException(message);
    }
    for (Square square : squares) {
      square.setStatus(SquareStatus.TAKEN);
      square.setOwnerName(customer.getDisplayName());
      square.setOwnerSessionId(request.getSessionId());
      square.setTpiCustomerId(customer.getCustomerId());
      square.setReservedBySessionId(null);
      square.setReservedUntil(null);
    }
    squareRepository.saveAll(squares);

    if (wasOpen && isBoardFull(boardId)) {
      lockBoard(board);
      createFollowUpBoard(board);
    }
    broadcastSnapshot(boardId);
  }

    @Transactional
    public void startGame(Long boardId) {
        Board board = loadBoard(boardId);
        long purchasedCount = squareRepository.countByBoardIdAndStatus(boardId, SquareStatus.TAKEN);
        int required = Math.max(board.getMinSquaresToActivate(), 35);
        if (purchasedCount < required) {
            refundAllBuyIns(board, (int) purchasedCount);
            resetBoard(boardId);
            throw new IllegalStateException(
                    "Board requires at least " + required + " squares. Refunds issued.");
        }
        board.setStatus(BoardStatus.STARTED);
        startGameClock(board);
        if (appProperties.isHouseOnLock()) {
            List<Square> squares = squareRepository.findByBoardId(boardId);
            for (Square square : squares) {
                if (square.getStatus() == SquareStatus.EMPTY || square.getStatus() == SquareStatus.RESERVED) {
                    square.setStatus(SquareStatus.HOUSE);
                    square.setOwnerName("HOUSE");
                    square.setOwnerSessionId(null);
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
    int purchasedCount =
        (int) squareRepository.countByBoardIdAndStatus(boardId, SquareStatus.TAKEN);
    int prizeCents = prizeCentsForQuarter(board, purchasedCount, quarter);
    int rollover = board.getRolloverCents();
    if (appProperties.isRolloverOnNoWinner() && rollover > 0) {
      prizeCents += rollover;
      board.setRolloverCents(0);
      board.getRolloverQuarters().clear();
    }
    boolean playerWinner =
        winner.getStatus() == SquareStatus.TAKEN
            && winner.getOwnerName() != null
            && !winner.getOwnerName().equalsIgnoreCase("HOUSE");
    if (playerWinner) {
      recordPayout(board, quarter, prizeCents, winnerIdx, winner.getOwnerName());
      creditWinner(board, winner, quarter, prizeCents);
    } else if (appProperties.isRolloverOnNoWinner()) {
      if (quarter == Quarter.Q4) {
        recordRefunds(board, prizeCents);
        board.setRolloverCents(0);
        board.getRolloverQuarters().clear();
      } else {
        board.setRolloverCents(board.getRolloverCents() + prizeCents);
        board.getRolloverQuarters().add(quarter);
        recordRollover(board, quarter, prizeCents, winnerIdx);
      }
    } else {
      recordPayout(board, quarter, prizeCents, winnerIdx, "HOUSE");
    }
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
        board.setRolloverCents(0);
        board.getRolloverQuarters().clear();
        board.setCurrentQuarter(Quarter.Q1);
        board.getConfirmedQuarters().clear();
        boardRepository.save(board);

    List<Square> squares = squareRepository.findByBoardId(boardId);
    for (Square square : squares) {
      square.setStatus(SquareStatus.EMPTY);
      square.setOwnerName(null);
      square.setOwnerSessionId(null);
      square.setReservedBySessionId(null);
      square.setReservedUntil(null);
      square.setTpiCustomerId(null);
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

  private void createFollowUpBoard(Board board) {
    if (board.getGameId() == null || board.getGameId().isBlank()) {
      return;
    }
    long existing =
        boardRepository.countByGameIdAndPriceCents(board.getGameId(), board.getPriceCents());
    int nextIndex = (int) existing + 1;
    String baseName =
        board.getGameName() != null && !board.getGameName().isBlank()
            ? board.getGameName()
            : "Board";
    String boardLabel = baseName.equals("Board") ? "Board" : baseName + " Board";
    Board nextBoard = new Board();
    nextBoard.setName(String.format("%s #%d", boardLabel, nextIndex));
    nextBoard.setSportType(board.getSportType());
    nextBoard.setGameName(board.getGameName());
    nextBoard.setGameId(board.getGameId());
    nextBoard.setHomeTeam(board.getHomeTeam());
    nextBoard.setAwayTeam(board.getAwayTeam());
    nextBoard.setPriceCents(board.getPriceCents());
    nextBoard.setHousePercent(board.getHousePercent());
    nextBoard.setMinSquaresToActivate(board.getMinSquaresToActivate());
    nextBoard.setPayoutQ1Percent(board.getPayoutQ1Percent());
    nextBoard.setPayoutQ2Percent(board.getPayoutQ2Percent());
    nextBoard.setPayoutQ3Percent(board.getPayoutQ3Percent());
    nextBoard.setPayoutQ4Percent(board.getPayoutQ4Percent());
    Board saved = boardRepository.save(nextBoard);
    initializeSquares(saved);
  }

  private void ensureDigits(Board board) {
    if (board.getRowDigits() == null || board.getColDigits() == null) {
      board.setRowDigits(joinDigits(generateDigits()));
      board.setColDigits(joinDigits(generateDigits()));
    }
  }

  private boolean isBoardFull(Long boardId) {
    List<Square> squares = squareRepository.findByBoardId(boardId);
    return squares.stream()
        .noneMatch(
            square ->
                square.getStatus() == SquareStatus.EMPTY
                    || square.getStatus() == SquareStatus.RESERVED);
  }

  private Board loadBoard(Long boardId) {
    return boardRepository
        .findById(boardId)
        .orElseThrow(() -> new IllegalArgumentException("Board not found"));
  }

  private Square loadSquare(Long boardId, int idx) {
    return squareRepository
        .findByBoardIdAndIdx(boardId, idx)
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
        snapshot.setRolloverOnNoWinner(appProperties.isRolloverOnNoWinner());
        snapshot.setRolloverCents(board.getRolloverCents());
        snapshot.setRolloverQuarters(board.getRolloverQuarters());
        int purchasedCount = (int) squares.stream().filter(square -> square.getStatus() == SquareStatus.TAKEN).count();
        snapshot.setPurchasedCount(purchasedCount);
        snapshot.setActive(purchasedCount >= board.getMinSquaresToActivate());
        applyPrizeBreakdown(snapshot, board, squares, purchasedCount);
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
        long deltaSeconds = Math.max(0, Duration.between(lastUpdate, now).getSeconds());
        if (deltaSeconds == 0) {
            return false;
        }
        board.setGameClockSeconds(board.getGameClockSeconds() + (int) deltaSeconds);
        board.setGameClockUpdatedAt(now);
        board.setGameClock(formatClock(board.getGameClockSeconds()));
        return true;
    }

  private void applyPrizeBreakdown(BoardSnapshot snapshot,
                                   Board board,
                                   List<Square> squares,
                                   int purchasedCount) {
    PrizeBreakdown breakdown = computePrizeBreakdown(board, squares, purchasedCount);
    snapshot.setPrizePoolCents(breakdown.prizePoolCents());
    snapshot.setPrizeQ1Cents(breakdown.prizeQ1Cents());
    snapshot.setPrizeQ2Cents(breakdown.prizeQ2Cents());
    snapshot.setPrizeQ3Cents(breakdown.prizeQ3Cents());
    snapshot.setPrizeQ4Cents(breakdown.prizeQ4Cents());
    snapshot.setPrizePerSquareQ1Cents(breakdown.prizePerSquareQ1Cents());
    snapshot.setPrizePerSquareQ2Cents(breakdown.prizePerSquareQ2Cents());
    snapshot.setPrizePerSquareQ3Cents(breakdown.prizePerSquareQ3Cents());
    snapshot.setPrizePerSquareQ4Cents(breakdown.prizePerSquareQ4Cents());
    snapshot.setPrizeQ1RolledOver(breakdown.prizeQ1RolledOver());
    snapshot.setPrizeQ2RolledOver(breakdown.prizeQ2RolledOver());
    snapshot.setPrizeQ3RolledOver(breakdown.prizeQ3RolledOver());
    snapshot.setFinalPrizeRefunded(breakdown.finalPrizeRefunded());
    snapshot.setFinalRefundPerPlayerCents(breakdown.finalRefundPerPlayerCents());
  }

    private PrizeBreakdown computePrizeBreakdown(Board board, List<Square> squares, int purchasedCount) {
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

        boolean q1Rolled = false;
        boolean q2Rolled = false;
        boolean q3Rolled = false;
        int rolloverToFinal = 0;
        if (isConfirmed(board, Quarter.Q1) && isWinningSquareEmpty(squares, Quarter.Q1)) {
            rolloverToFinal += q1;
            q1 = 0;
            q1Rolled = true;
        }
        if (isConfirmed(board, Quarter.Q2) && isWinningSquareEmpty(squares, Quarter.Q2)) {
            rolloverToFinal += q2;
            q2 = 0;
            q2Rolled = true;
        }
        if (isConfirmed(board, Quarter.Q3) && isWinningSquareEmpty(squares, Quarter.Q3)) {
            rolloverToFinal += q3;
            q3 = 0;
            q3Rolled = true;
        }
        q4 += rolloverToFinal;

        boolean finalEmpty = isConfirmed(board, Quarter.Q4) && isWinningSquareEmpty(squares, Quarter.Q4);
        int finalRefundPerPlayerCents = 0;
        int perSquareQ4 = perSquareAmount(q4);
        if (finalEmpty) {
            int rake = percentOf(q4, FINAL_EMPTY_RAKE_PERCENT);
            int refundPool = Math.max(0, q4 - rake);
            long playerCount =
                squares.stream()
                    .filter(square -> square.getStatus() == SquareStatus.TAKEN)
                    .map(Square::getOwnerSessionId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .count();
            if (playerCount > 0) {
                finalRefundPerPlayerCents = (int) (refundPool / playerCount);
            }
            perSquareQ4 = 0;
        }

        return new PrizeBreakdown(
            (int) pool,
            q1,
            q2,
            q3,
            q4,
            perSquareAmount(q1),
            perSquareAmount(q2),
            perSquareAmount(q3),
            perSquareQ4,
            q1Rolled,
            q2Rolled,
            q3Rolled,
            finalEmpty,
            finalRefundPerPlayerCents
        );
    }

    private boolean isConfirmed(Board board, Quarter quarter) {
        return board.getConfirmedQuarters() != null && board.getConfirmedQuarters().contains(quarter);
    }

    private boolean isWinningSquareEmpty(List<Square> squares, Quarter quarter) {
        Square winner = findWinnerSquare(squares, quarter);
        if (winner == null) {
            return false;
        }
        return winner.getStatus() != SquareStatus.TAKEN && winner.getStatus() != SquareStatus.HOUSE;
    }

    private int calculateWinnings(String sessionId,
                                  int ownedCount,
                                  List<Square> boardSquares,
                                  PrizeBreakdown breakdown) {
        int total = 0;
        total += payoutForQuarter(sessionId, ownedCount, boardSquares, Quarter.Q1,
            breakdown.prizePerSquareQ1Cents());
        total += payoutForQuarter(sessionId, ownedCount, boardSquares, Quarter.Q2,
            breakdown.prizePerSquareQ2Cents());
        total += payoutForQuarter(sessionId, ownedCount, boardSquares, Quarter.Q3,
            breakdown.prizePerSquareQ3Cents());
        if (breakdown.finalPrizeRefunded()) {
            total += breakdown.finalRefundPerPlayerCents();
            return total;
        }
        total += payoutForQuarter(sessionId, ownedCount, boardSquares, Quarter.Q4,
            breakdown.prizePerSquareQ4Cents());
        return total;
    }

    private int payoutForQuarter(String sessionId,
                                 int ownedCount,
                                 List<Square> boardSquares,
                                 Quarter quarter,
                                 int perSquareCents) {
        if (ownedCount <= 0 || perSquareCents <= 0) {
            return 0;
        }
        Square winner = findWinnerSquare(boardSquares, quarter);
        if (winner == null) {
            return 0;
        }
        if (!Objects.equals(sessionId, winner.getOwnerSessionId())) {
            return 0;
        }
        return perSquareCents * ownedCount;
    }

    private Square findWinnerSquare(List<Square> squares, Quarter quarter) {
        return squares.stream()
            .filter(square -> switch (quarter) {
                case Q1 -> square.isWonQ1();
                case Q2 -> square.isWonQ2();
                case Q3 -> square.isWonQ3();
                case Q4 -> square.isWonFinal();
            })
            .findFirst()
            .orElse(null);
    }

    private int perSquareAmount(int totalCents) {
        if (totalCents <= 0) {
            return 0;
        }
        return (int) Math.round(totalCents / (double) TOTAL_SQUARES);
    }

    private int percentOf(long total, double percent) {
        if (total <= 0 || percent <= 0) {
            return 0;
        }
        return (int) Math.round(total * (percent / 100.0));
    }

    private int prizeCentsForQuarter(Board board, int purchasedCount, Quarter quarter) {
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
        return switch (quarter) {
            case Q1 -> q1;
            case Q2 -> q2;
            case Q3 -> q3;
            case Q4 -> q4;
        };
    }

    private void creditWinner(Board board, Square winner, Quarter quarter, int amountCents) {
        if (amountCents <= 0) {
            log.info("Skipping TPI credit: zero payout for board {} quarter {}", board.getId(), quarter);
            return;
        }
        if (winner.getTpiCustomerId() == null || winner.getTpiCustomerId().isBlank()) {
            log.warn("Skipping TPI credit: missing customer id for winner square {} on board {}", winner.getIdx(), board.getId());
            return;
        }
        DebitRequest creditRequest = buildCreditRequest(board, winner, amountCents);
        log.info("TPI credit start board={} quarter={} square={} customer={} amountCents={}", board.getId(), quarter, winner.getIdx(), winner.getTpiCustomerId(), amountCents);
        DebitResponse response = tpiClient.credit(creditRequest);
        if (response.getResponseCode() != null && response.getResponseCode() != 0) {
            String message = response.getResponseMessage() != null ? response.getResponseMessage() : "Credit declined";
            log.warn("TPI credit declined board={} quarter={} square={} code={} message={}", board.getId(), quarter, winner.getIdx(), response.getResponseCode(), message);
            return;
        }
        log.info("TPI credit success board={} quarter={} square={} transactionId={} balance={}", board.getId(), quarter, winner.getIdx(), response.getAleaTransactionId(), response.getAleaAccountBalance() != null ? response.getAleaAccountBalance().getValue() : null);
    }

    private void recordPayout(Board board, Quarter quarter, int amountCents, Integer winnerIdx, String recipient) {
        PayoutEvent event = new PayoutEvent();
        event.setBoard(board);
        event.setQuarter(quarter);
        event.setEventType(PayoutEventType.PAY);
        event.setRecipient(recipient);
        event.setAmountCents(amountCents);
        event.setWinnerSquareIdx(winnerIdx);
        payoutEventRepository.save(event);
    }

    private void recordRefunds(Board board, int amountCents) {
        if (amountCents <= 0) {
            return;
        }
        List<Square> squares = squareRepository.findByBoardId(board.getId());
        Map<String, Integer> counts = new HashMap<>();
        for (Square square : squares) {
            if (square.getStatus() != SquareStatus.TAKEN) {
                continue;
            }
            String owner = square.getOwnerName();
            if (owner == null || owner.isBlank() || owner.equalsIgnoreCase("HOUSE")) {
                continue;
            }
            counts.merge(owner, 1, Integer::sum);
        }
        int total = counts.values().stream().mapToInt(Integer::intValue).sum();
        if (total == 0) {
            return;
        }
        class Share {
            final String owner;
            final int amount;
            final int remainder;
            Share(String owner, int amount, int remainder) {
                this.owner = owner;
                this.amount = amount;
                this.remainder = remainder;
            }
        }
        List<Share> shares = new ArrayList<>();
        int allocated = 0;
        for (Map.Entry<String, Integer> entry : counts.entrySet()) {
            long numerator = (long) amountCents * entry.getValue();
            int share = (int) (numerator / total);
            int remainder = (int) (numerator % total);
            allocated += share;
            shares.add(new Share(entry.getKey(), share, remainder));
        }
        int leftover = amountCents - allocated;
        shares.sort(Comparator.<Share>comparingInt(s -> s.remainder).reversed()
                .thenComparing(s -> s.owner));
        int index = 0;
        while (leftover > 0 && !shares.isEmpty()) {
            Share share = shares.get(index);
            shares.set(index, new Share(share.owner, share.amount + 1, share.remainder));
            leftover--;
            index = (index + 1) % shares.size();
        }
        for (Share share : shares) {
            if (share.amount <= 0) {
                continue;
            }
            recordPayout(board, Quarter.Q4, share.amount, null, share.owner);
        }
    }

    private void refundAllBuyIns(Board board, int purchasedCount) {
        if (purchasedCount <= 0) {
            return;
        }
        int totalRefundCents = purchasedCount * board.getPriceCents();
        recordRefunds(board, totalRefundCents);
    }

    private void recordRollover(Board board, Quarter quarter, int amountCents, int winnerIdx) {
        PayoutEvent event = new PayoutEvent();
        event.setBoard(board);
        event.setQuarter(quarter);
        event.setEventType(PayoutEventType.ROLLOVER);
        event.setRecipient("ROLLOVER");
        event.setAmountCents(amountCents);
        event.setWinnerSquareIdx(winnerIdx);
        payoutEventRepository.save(event);
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
            long deltaSeconds = Math.max(0, Duration.between(board.getGameClockUpdatedAt(), now).getSeconds());
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
    snapshot.setOwnerSessionId(square.getOwnerSessionId());
    snapshot.setReservedBySessionId(square.getReservedBySessionId());
    snapshot.setReservedUntil(square.getReservedUntil());
    snapshot.setWonQ1(square.isWonQ1());
    snapshot.setWonQ2(square.isWonQ2());
    snapshot.setWonQ3(square.isWonQ3());
    snapshot.setWonFinal(square.isWonFinal());
    return snapshot;
  }

  private PlayerHistorySquare toPlayerHistorySquare(Square square) {
    PlayerHistorySquare snapshot = new PlayerHistorySquare();
    snapshot.setIdx(square.getIdx());
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

  private void initializeSquares(Board board) {
    List<Square> squares = new ArrayList<>();
    for (int row = 0; row < 10; row++) {
      for (int col = 0; col < 10; col++) {
        Square square = new Square();
        square.setBoard(board);
        square.setRowIndex(row);
        square.setColIndex(col);
        square.setIdx(row * 10 + col);
        square.setStatus(SquareStatus.EMPTY);
        squares.add(square);
      }
    }
    squareRepository.saveAll(squares);
  }

  private LobbyGameResponse toLobbyGame(Board board) {
    LobbyGameResponse response = new LobbyGameResponse();
    response.setGameId(board.getGameId());
    response.setName(
        board.getGameName() != null && !board.getGameName().isBlank()
            ? board.getGameName()
            : String.format("%s vs %s", board.getHomeTeam(), board.getAwayTeam()));
    response.setHomeTeam(board.getHomeTeam());
    response.setAwayTeam(board.getAwayTeam());
    return response;
  }

  private LobbyBoardResponse toLobbyBoard(Board board) {
    LobbyBoardResponse response = new LobbyBoardResponse();
    response.setId(board.getId());
    response.setName(board.getName());
    response.setStatus(board.getStatus());
    int openSquares = countOpenSquares(board.getId());
    response.setOpenSquares(openSquares);
    response.setTotalSquares(100);
    response.setFull(openSquares == 0);
    return response;
  }

  public AdminBoardSummary toAdminSummary(Board board) {
    AdminBoardSummary summary = new AdminBoardSummary();
    summary.setId(board.getId());
    summary.setName(board.getName());
    summary.setSportType(board.getSportType());
    summary.setGameName(board.getGameName());
    summary.setGameId(board.getGameId());
    summary.setHomeTeam(board.getHomeTeam());
    summary.setAwayTeam(board.getAwayTeam());
    summary.setPriceCents(board.getPriceCents());
    summary.setHousePercent(board.getHousePercent());
    summary.setMinSquaresToActivate(board.getMinSquaresToActivate());
    summary.setPayoutQ1Percent(board.getPayoutQ1Percent());
    summary.setPayoutQ2Percent(board.getPayoutQ2Percent());
    summary.setPayoutQ3Percent(board.getPayoutQ3Percent());
    summary.setPayoutQ4Percent(board.getPayoutQ4Percent());
    summary.setStatus(board.getStatus());
    summary.setOpenSquares(countOpenSquares(board.getId()));
    return summary;
  }

  private int countOpenSquares(Long boardId) {
    long empty = squareRepository.countByBoardIdAndStatus(boardId, SquareStatus.EMPTY);
    long reserved = squareRepository.countByBoardIdAndStatus(boardId, SquareStatus.RESERVED);
    return (int) (empty + reserved);
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

  private DebitRequest buildCreditRequest(Board board, Square winner, long amountCents) {
    DebitRequest debitRequest = new DebitRequest();
    debitRequest.setThirdPartyTransactionTypeId(tpiProperties.getThirdPartyTransactionTypeId());
    debitRequest.setThirdPartyTransactionId("credit-" + UUID.randomUUID());
    debitRequest.setThirdPartyRoundId(String.valueOf(board.getId()));
    debitRequest.setCustomerId(winner.getTpiCustomerId());
    debitRequest.setGameTypeId(tpiProperties.getGameTypeId());
    debitRequest.setGameTypeVariationId(tpiProperties.getGameTypeVariationId());
    debitRequest.setAmount(new MoneyAmount(tpiProperties.getCurrency(), amountCents));
    debitRequest.setRoundToBeClosed(tpiProperties.isRoundToBeClosed());
    return debitRequest;
  }

    private record PrizeBreakdown(int prizePoolCents,
                                  int prizeQ1Cents,
                                  int prizeQ2Cents,
                                  int prizeQ3Cents,
                                  int prizeQ4Cents,
                                  int prizePerSquareQ1Cents,
                                  int prizePerSquareQ2Cents,
                                  int prizePerSquareQ3Cents,
                                  int prizePerSquareQ4Cents,
                                  boolean prizeQ1RolledOver,
                                  boolean prizeQ2RolledOver,
                                  boolean prizeQ3RolledOver,
                                  boolean finalPrizeRefunded,
                                  int finalRefundPerPlayerCents) {
    }
}
