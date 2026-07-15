package com.mariafernandes.sprintly.controller;

import com.mariafernandes.sprintly.domain.Team;
import com.mariafernandes.sprintly.repository.TeamRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/teams")
public class TeamController {

    private final TeamRepository repository;

    public TeamController(TeamRepository repository) {
        this.repository = repository;
    }

    @PostMapping
    public Team create(@RequestBody Team team) {
        return repository.save(team);
    }

    @GetMapping
    public List<Team> findByOrganization(@RequestParam Long organizationId) {
        return repository.findByOrganizationId(organizationId);
    }
}