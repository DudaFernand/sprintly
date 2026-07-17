package com.mariafernandes.sprintly.controller;

import com.mariafernandes.sprintly.domain.Status;
import com.mariafernandes.sprintly.domain.User;
import com.mariafernandes.sprintly.service.StatusService;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/status")
public class StatusController {

    private final StatusService service;

    public StatusController(StatusService service) {
        this.service = service;
    }

    @PostMapping
    public Status create(@RequestBody Status status, @AuthenticationPrincipal User currentUser) {
        return service.create(status, currentUser);
    }

    @GetMapping
    public List<Status> findByBoard(@RequestParam Long boardId, @AuthenticationPrincipal User currentUser) {
        return service.findByBoard(boardId, currentUser);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id,
                       @RequestParam Long boardId,
                       @AuthenticationPrincipal User currentUser) {
        service.delete(id, boardId, currentUser);
    }
}