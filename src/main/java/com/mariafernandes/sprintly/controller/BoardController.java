package com.mariafernandes.sprintly.controller;

import com.mariafernandes.sprintly.domain.Board;
import com.mariafernandes.sprintly.domain.User;
import com.mariafernandes.sprintly.service.BoardService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/boards")
public class BoardController {

    private final BoardService service;

    public BoardController(BoardService service) {
        this.service = service;
    }

    @PostMapping
    public Board create(@RequestBody Board board, @AuthenticationPrincipal User currentUser) {
        return service.create(board, currentUser);
    }

    @GetMapping
    public List<Board> findByProject(@RequestParam Long projectId, @AuthenticationPrincipal User currentUser) {
        return service.findByProject(projectId, currentUser);
    }
}