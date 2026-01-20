package com.example.squarespool.controller;

import com.example.squarespool.service.BoardService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class ViewController {
    private final BoardService boardService;

    public ViewController(BoardService boardService) {
        this.boardService = boardService;
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("boards", boardService.listBoards());
        return "index";
    }

    @GetMapping("/boards/{id}/view")
    public String board(@PathVariable Long id, Model model) {
        model.addAttribute("boardId", id);
        return "board";
    }

    @GetMapping("/admin")
    public String admin() {
        return "admin";
    }
}
