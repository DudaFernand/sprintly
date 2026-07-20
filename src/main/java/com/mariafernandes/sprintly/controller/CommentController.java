package com.mariafernandes.sprintly.controller;

import com.mariafernandes.sprintly.domain.Comment;
import com.mariafernandes.sprintly.domain.User;
import com.mariafernandes.sprintly.dto.CreateCommentRequest;
import com.mariafernandes.sprintly.service.CommentService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/comments")
public class CommentController {

    private final CommentService service;

    public CommentController(CommentService service) {
        this.service = service;
    }

    @PostMapping
    public Comment create(@Valid @RequestBody CreateCommentRequest request, @AuthenticationPrincipal User author) {
        return service.create(request, author);
    }

    @GetMapping
    public List<Comment> findByTask(@RequestParam Long taskId, @AuthenticationPrincipal User currentUser) {
        return service.findByTask(taskId, currentUser);
    }
}