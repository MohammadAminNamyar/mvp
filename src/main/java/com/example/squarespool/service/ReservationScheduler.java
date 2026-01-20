package com.example.squarespool.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ReservationScheduler {
    private final BoardService boardService;

    public ReservationScheduler(BoardService boardService) {
        this.boardService = boardService;
    }

    @Scheduled(fixedDelayString = "${squares.reservationSweepMs:2000}")
    public void sweepExpiredReservations() {
        Map<Long, Integer> touched = boardService.releaseExpiredReservations();
        for (Long boardId : touched.keySet()) {
            boardService.broadcastSnapshot(boardId);
        }
    }
}
