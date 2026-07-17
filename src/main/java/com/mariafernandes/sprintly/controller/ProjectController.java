package com.mariafernandes.sprintly.controller;

import com.mariafernandes.sprintly.domain.Project;
import com.mariafernandes.sprintly.domain.User;
import com.mariafernandes.sprintly.service.ProjectService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/projects")
public class ProjectController {

    private final ProjectService service;

    public ProjectController(ProjectService service) {
        this.service = service;
    }

    @PostMapping
    public Project create(@RequestBody Project project, @AuthenticationPrincipal User currentUser) {
        return service.create(project, currentUser);
    }

    @GetMapping
    public List<Project> findByTeam(@RequestParam Long teamId, @AuthenticationPrincipal User currentUser) {
        return service.findByTeam(teamId, currentUser);
    }
}