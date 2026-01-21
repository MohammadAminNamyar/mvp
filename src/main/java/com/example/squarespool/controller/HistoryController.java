package com.example.squarespool.controller;

import com.example.squarespool.dto.PlayerHistoryBoard;
import com.example.squarespool.service.BoardService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/history")
public class HistoryController {
    private final BoardService boardService;

    public HistoryController(BoardService boardService) {
        this.boardService = boardService;
    }

    @GetMapping
    public List<PlayerHistoryBoard> history(
        @RequestHeader(value = "X-Session-Id", required = false) String sessionId) {
        return boardService.getPlayerHistory(sessionId);
    }
}
