package com.mariafernandes.sprintly.controller;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mariafernandes.sprintly.domain.Sprint;
import com.mariafernandes.sprintly.domain.User;
import com.mariafernandes.sprintly.dto.BurndownResponse;
import com.mariafernandes.sprintly.service.BurndownService;
import com.mariafernandes.sprintly.service.SprintService;

@RestController
@RequestMapping("/sprints")
public class SprintController {

    private final SprintService service;
    private final BurndownService burndownService;

    public SprintController(SprintService service, BurndownService burndownService) {
        this.service = service;
        this.burndownService = burndownService;
    }

    @PostMapping
    public Sprint create(@RequestBody Sprint sprint, @AuthenticationPrincipal User currentUser) {
        return service.create(sprint, currentUser);
    }

    @GetMapping
    public List<Sprint> findByProject(@RequestParam Long projectId, @AuthenticationPrincipal User currentUser) {
        return service.findByProject(projectId, currentUser);
    }

    @GetMapping("/{id}/burndown")
    public BurndownResponse calculateBurndown(@PathVariable Long id, @AuthenticationPrincipal User currentUser) {
        return burndownService.calculate(id, currentUser);
    }
}
