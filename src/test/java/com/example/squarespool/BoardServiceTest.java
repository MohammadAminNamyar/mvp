package com.example.squarespool;

import com.example.squarespool.dto.CreateBoardRequest;
import com.example.squarespool.dto.PurchaseRequest;
import com.example.squarespool.model.Board;
import com.example.squarespool.model.Square;
import com.example.squarespool.repository.SquareRepository;
import com.example.squarespool.service.BoardService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.test.annotation.DirtiesContext;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class BoardServiceTest {
    @Autowired
    private BoardService boardService;

    @Autowired
    private SquareRepository squareRepository;

    @Test
    void generatesPermutationDigits() {
        List<Integer> digits = boardService.generateDigits();
        assertThat(digits).hasSize(10);
        assertThat(digits).doesNotHaveDuplicates();
        assertThat(digits).containsExactlyInAnyOrder(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    }

    @Test
    void computesWinnerIdxFromScores() {
        Board board = new Board();
        board.setRowDigits("0,1,2,3,4,5,6,7,8,9");
        board.setColDigits("9,8,7,6,5,4,3,2,1,0");
        board.setHomeScore(17);
        board.setAwayScore(3);

        int winnerIdx = boardService.computeWinnerIdx(board);
        assertThat(winnerIdx).isEqualTo(76);
    }

    @Test
    @DirtiesContext
    void onlyOneReservationSucceeds() throws Exception {
        Board board = boardService.createBoard(sampleBoardRequest());
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger successCount = new AtomicInteger();

        Future<?> first = executor.submit(() -> {
            ready.countDown();
            awaitLatch(start);
            try {
                boardService.reserveSquare(board.getId(), 0, "session-a");
                successCount.incrementAndGet();
            } catch (OptimisticLockingFailureException | IllegalStateException ignored) {
            }
        });

        Future<?> second = executor.submit(() -> {
            ready.countDown();
            awaitLatch(start);
            try {
                boardService.reserveSquare(board.getId(), 0, "session-b");
                successCount.incrementAndGet();
            } catch (OptimisticLockingFailureException | IllegalStateException ignored) {
            }
        });

        ready.await(5, TimeUnit.SECONDS);
        start.countDown();
        first.get(5, TimeUnit.SECONDS);
        second.get(5, TimeUnit.SECONDS);
        executor.shutdownNow();

        assertThat(successCount.get()).isEqualTo(1);
    }

    @Test
    @DirtiesContext
    void purchaseRequiresReservationBySameSessionAndNotExpired() {
        Board board = boardService.createBoard(sampleBoardRequest());
        boardService.reserveSquare(board.getId(), 5, "session-a");

        PurchaseRequest wrongSession = new PurchaseRequest();
        wrongSession.setSessionId("session-b");
        wrongSession.setCustomerName("Other");
        wrongSession.setIndices(List.of(5));

        assertThatThrownBy(() -> boardService.purchase(board.getId(), wrongSession))
                .isInstanceOf(IllegalStateException.class);

        Square square = squareRepository.findByBoardIdAndIdx(board.getId(), 5)
                .orElseThrow();
        square.setReservedUntil(Instant.now().minusSeconds(5));
        squareRepository.save(square);

        PurchaseRequest expired = new PurchaseRequest();
        expired.setSessionId("session-a");
        expired.setCustomerName("Test");
        expired.setIndices(List.of(5));

        assertThatThrownBy(() -> boardService.purchase(board.getId(), expired))
                .isInstanceOf(IllegalStateException.class);
    }

    private void awaitLatch(CountDownLatch latch) {
        try {
            latch.await(5, TimeUnit.SECONDS);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }

    private CreateBoardRequest sampleBoardRequest() {
        CreateBoardRequest request = new CreateBoardRequest();
        request.setName("Demo");
        request.setHomeTeam("Home");
        request.setAwayTeam("Away");
        request.setPriceCents(500);
        request.setHousePercent(10);
        request.setMinSquaresToActivate(0);
        return request;
    }
}
