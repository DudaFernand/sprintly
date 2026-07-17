package com.mariafernandes.sprintly.controller;

import com.mariafernandes.sprintly.domain.Board;
import com.mariafernandes.sprintly.repository.BoardRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/boards")
public class BoardController {

    private final BoardRepository repository;

    public BoardController(BoardRepository repository) {
        this.repository = repository;
    }

    @PostMapping
    public Board create(@RequestBody Board board) {
        return repository.save(board);
    }

    @GetMapping
    public List<Board> findByProject(@RequestParam Long projectId) {
        return repository.findByProjectId(projectId);
    }
}