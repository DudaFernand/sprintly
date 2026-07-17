package com.mariafernandes.sprintly.controller;

import com.mariafernandes.sprintly.domain.Team;
import com.mariafernandes.sprintly.domain.User;
import com.mariafernandes.sprintly.service.TeamService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/teams")
public class TeamController {

    private final TeamService service;

    public TeamController(TeamService service) {
        this.service = service;
    }

    @PostMapping
    public Team create(@RequestBody Team team, @AuthenticationPrincipal User currentUser) {
        return service.create(team, currentUser);
    }

    @GetMapping
    public List<Team> findByOrganization(@RequestParam Long organizationId, @AuthenticationPrincipal User currentUser) {
        return service.findByOrganization(organizationId, currentUser);
    }
}