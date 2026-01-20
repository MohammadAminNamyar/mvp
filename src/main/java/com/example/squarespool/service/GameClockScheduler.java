package com.example.squarespool.service;

import com.example.squarespool.model.Board;
import com.example.squarespool.model.BoardStatus;
import com.example.squarespool.repository.BoardRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Component
public class GameClockScheduler {
    private final BoardRepository boardRepository;
    private final BoardService boardService;

    public GameClockScheduler(BoardRepository boardRepository, BoardService boardService) {
        this.boardRepository = boardRepository;
        this.boardService = boardService;
    }

    @Scheduled(fixedDelayString = "${squares.clockTickMs:1000}")
    @Transactional
    public void tickClocks() {
        List<Board> boards = boardRepository.findByStatus(BoardStatus.STARTED);
        if (boards.isEmpty()) {
            return;
        }
        Instant now = Instant.now();
        for (Board board : boards) {
            if (boardService.tickGameClock(board, now)) {
                boardService.broadcastSnapshot(board.getId());
            }
        }
    }
}
