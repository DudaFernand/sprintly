package com.mariafernandes.sprintly.controller;

import com.mariafernandes.sprintly.domain.Task;
import com.mariafernandes.sprintly.domain.User;
import com.mariafernandes.sprintly.dto.CreateTaskRequest;
import com.mariafernandes.sprintly.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    private final TaskService service;

    public TaskController(TaskService service) {
        this.service = service;
    }

    @PostMapping
    public Task create(@Valid @RequestBody CreateTaskRequest request, @AuthenticationPrincipal User reporter) {
        return service.create(request, reporter);
    }

    @GetMapping
    public List<Task> findByBoard(@RequestParam Long boardId, @AuthenticationPrincipal User currentUser) {
        return service.findByBoard(boardId, currentUser);
    }

    @PatchMapping("/{id}/status")
    public Task updateStatus(@PathVariable Long id, @RequestBody Map<String, Long> body, @AuthenticationPrincipal User currentUser) {
        Long statusId = body.get("statusId");
        if (statusId == null) {
            throw new IllegalArgumentException("statusId é obrigatório");
        }
        return service.updateStatus(id, statusId, currentUser);
    }

    @PostMapping("/{taskId}/labels/{labelId}")
    public Task addLabel(@PathVariable Long taskId, @PathVariable Long labelId,
                         @AuthenticationPrincipal User currentUser) {
        return service.addLabel(taskId, labelId, currentUser);
    }

    @DeleteMapping("/{taskId}/labels/{labelId}")
    public Task removeLabel(@PathVariable Long taskId, @PathVariable Long labelId,
                            @AuthenticationPrincipal User currentUser) {
        return service.removeLabel(taskId, labelId, currentUser);
    }

    @PatchMapping("/{taskId}/sprint")
    public Task assignToSprint(@PathVariable Long taskId,
                               @RequestBody Map<String, Long> body,
                               @AuthenticationPrincipal User currentUser) {
        return service.assignToSprint(taskId, body.get("sprintId"), currentUser);
    }
}
