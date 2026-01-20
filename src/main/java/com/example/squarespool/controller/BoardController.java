package com.example.squarespool.controller;

import com.example.squarespool.dto.BoardSnapshot;
import com.example.squarespool.dto.PurchaseRequest;
import com.example.squarespool.dto.ReserveRequest;
import com.example.squarespool.service.BoardService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/boards")
public class BoardController {
    private final BoardService boardService;

    public BoardController(BoardService boardService) {
        this.boardService = boardService;
    }

    @GetMapping("/{id}")
    public BoardSnapshot getBoard(@PathVariable Long id) {
        return boardService.getSnapshot(id);
    }

    @PostMapping("/{id}/reserve/{idx}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reserve(@PathVariable Long id, @PathVariable int idx, @Valid @RequestBody ReserveRequest request) {
        boardService.reserveSquare(id, idx, request.getSessionId());
    }

    @PostMapping("/{id}/unreserve/{idx}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unreserve(@PathVariable Long id, @PathVariable int idx, @Valid @RequestBody ReserveRequest request) {
        boardService.unreserveSquare(id, idx, request.getSessionId());
    }

    @PostMapping("/{id}/purchase")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void purchase(@PathVariable Long id, @Valid @RequestBody PurchaseRequest request) {
        boardService.purchase(id, request);
    }
}
