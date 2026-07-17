package com.mariafernandes.sprintly.controller;

import com.mariafernandes.sprintly.domain.Status;
import com.mariafernandes.sprintly.service.StatusService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/statuses")
public class StatusController {

    private final StatusService service;

    public StatusController(StatusService service) {
        this.service = service;
    }

    @PostMapping
    public Status create(@RequestBody Status status) {
        return service.create(status);
    }

    @GetMapping
    public List<Status> findByBoard(@RequestParam Long boardId) {
        return service.findByBoard(boardId);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}