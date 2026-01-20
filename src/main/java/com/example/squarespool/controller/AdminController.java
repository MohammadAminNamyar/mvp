package com.example.squarespool.controller;

import com.example.squarespool.config.AppProperties;
import com.example.squarespool.dto.AdminBoardResponse;
import com.example.squarespool.dto.AdminSettingsRequest;
import com.example.squarespool.dto.AdminSettingsResponse;
import com.example.squarespool.dto.ConfirmQuarterRequest;
import com.example.squarespool.dto.CreateBoardRequest;
import com.example.squarespool.dto.ScoreUpdateRequest;
import com.example.squarespool.dto.UpdateBoardRequest;
import com.example.squarespool.model.Board;
import com.example.squarespool.service.BoardService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/admin")
public class AdminController {
    private final BoardService boardService;
    private final AppProperties appProperties;

    public AdminController(BoardService boardService, AppProperties appProperties) {
        this.boardService = boardService;
        this.appProperties = appProperties;
    }

    @PostMapping("/boards")
    public Board createBoard(@RequestHeader("X-Admin-Token") String token,
                             @Valid @RequestBody CreateBoardRequest request) {
        validateToken(token);
        return boardService.createBoard(request);
    }

    @GetMapping("/boards")
    public List<AdminBoardResponse> listBoards(@RequestHeader("X-Admin-Token") String token) {
        validateToken(token);
        return boardService.listBoardsForAdmin();
    }

    @PutMapping("/boards/{id}")
    public Board updateBoard(@RequestHeader("X-Admin-Token") String token,
                             @PathVariable Long id,
                             @Valid @RequestBody UpdateBoardRequest request) {
        validateToken(token);
        return boardService.updateBoard(id, request);
    }

    @DeleteMapping("/boards/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBoard(@RequestHeader("X-Admin-Token") String token, @PathVariable Long id) {
        validateToken(token);
        boardService.deleteBoard(id);
    }

    @GetMapping("/settings")
    public AdminSettingsResponse getSettings(@RequestHeader("X-Admin-Token") String token) {
        validateToken(token);
        return new AdminSettingsResponse(appProperties.isShowPurchaserNames());
    }

    @PostMapping("/settings")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateSettings(@RequestHeader("X-Admin-Token") String token,
                               @Valid @RequestBody AdminSettingsRequest request) {
        validateToken(token);
        appProperties.setShowPurchaserNames(request.isShowPurchaserNames());
    }

    @PostMapping("/boards/{id}/start")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void start(@RequestHeader("X-Admin-Token") String token, @PathVariable Long id) {
        validateToken(token);
        boardService.startGame(id);
    }

    @PostMapping("/boards/{id}/score")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void score(@RequestHeader("X-Admin-Token") String token,
                      @PathVariable Long id,
                      @Valid @RequestBody ScoreUpdateRequest request) {
        validateToken(token);
        boardService.updateScore(id, request.getHomeScore(), request.getAwayScore());
    }

    @PostMapping("/boards/{id}/confirm-quarter")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void confirmQuarter(@RequestHeader("X-Admin-Token") String token,
                               @PathVariable Long id,
                               @Valid @RequestBody ConfirmQuarterRequest request) {
        validateToken(token);
        boardService.confirmQuarter(id, request.getQuarter());
    }

    @PostMapping("/boards/{id}/reset")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reset(@RequestHeader("X-Admin-Token") String token, @PathVariable Long id) {
        validateToken(token);
        boardService.resetBoard(id);
    }

    private void validateToken(String token) {
        if (!appProperties.getAdminToken().equals(token)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid admin token");
        }
    }
}
