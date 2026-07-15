package com.mariafernandes.sprintly.controller;

import com.mariafernandes.sprintly.domain.Project;
import com.mariafernandes.sprintly.repository.ProjectRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/projects")
public class ProjectController {

    private final ProjectRepository repository;

    public ProjectController(ProjectRepository repository) {
        this.repository = repository;
    }

    @PostMapping
    public Project create(@RequestBody Project project) {
        return repository.save(project);
    }

    @GetMapping
    public List<Project> findByTeam(@RequestParam Long teamId) {
        return repository.findByTeamId(teamId);
    }
}