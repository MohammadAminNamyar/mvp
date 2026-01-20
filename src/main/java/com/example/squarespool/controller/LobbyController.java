package com.example.squarespool.controller;

import com.example.squarespool.config.AppProperties;
import com.example.squarespool.dto.BetOptionResponse;
import com.example.squarespool.dto.BoardSummaryResponse;
import com.example.squarespool.dto.GameOptionResponse;
import com.example.squarespool.dto.SportOptionResponse;
import com.example.squarespool.service.BoardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/lobby")
public class LobbyController {
    private final BoardService boardService;
    private final AppProperties appProperties;

    public LobbyController(BoardService boardService, AppProperties appProperties) {
        this.boardService = boardService;
        this.appProperties = appProperties;
    }

    @GetMapping("/sports")
    public List<SportOptionResponse> sports() {
        return boardService.listSports();
    }

    @GetMapping("/bets")
    public List<BetOptionResponse> bets() {
        return appProperties.getBetAmounts().stream()
                .map(amount -> new BetOptionResponse(amount * 100, "$" + amount))
                .toList();
    }

    @GetMapping("/games")
    public List<GameOptionResponse> games(@RequestParam String sport) {
        return boardService.listGames(sport);
    }

    @GetMapping("/boards")
    public List<BoardSummaryResponse> boards(@RequestParam String sport,
                                             @RequestParam String game,
                                             @RequestParam int bet) {
        return boardService.listBoards(sport, game, bet);
    }
}
