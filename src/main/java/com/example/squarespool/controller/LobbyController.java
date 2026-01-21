package com.example.squarespool.controller;

import com.example.squarespool.dto.BetOption;
import com.example.squarespool.dto.LobbyBoardResponse;
import com.example.squarespool.dto.LobbyGameResponse;
import com.example.squarespool.service.BoardService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/lobby")
public class LobbyController {
    private final BoardService boardService;

    public LobbyController(BoardService boardService) {
        this.boardService = boardService;
    }

    @GetMapping("/sports")
    public List<String> listSports() {
        return List.of("NFL", "NBA", "NCAAB", "NCAAF");
    }

    @GetMapping("/sports/{sport}/games")
    public List<LobbyGameResponse> listGames(@PathVariable String sport) {
        return boardService.listGamesBySport(sport);
    }

    @GetMapping("/games/{gameId}/boards")
    public List<LobbyBoardResponse> listBoards(@PathVariable String gameId,
                                               @RequestParam int betCents) {
        return boardService.listBoardsByGameAndBet(gameId, betCents);
    }

    @GetMapping("/games/{gameId}/bets")
    public List<BetOption> listBetsByGame(@PathVariable String gameId) {
        return boardService.listBetOptionsByGame(gameId);
    }

    @GetMapping("/bets")
    public List<BetOption> listBets() {
        return boardService.listBetOptions();
    }
}
