package com.mariafernandes.sprintly.service;

import com.mariafernandes.sprintly.domain.Team;
import com.mariafernandes.sprintly.domain.User;
import com.mariafernandes.sprintly.repository.TeamRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TeamService {

    private final TeamRepository repository;
    private final AuthorizationService authorizationService;

    public TeamService(TeamRepository repository, AuthorizationService authorizationService) {
        this.repository = repository;
        this.authorizationService = authorizationService;
    }

    public Team create(Team team, User currentUser) {
        authorizationService.requireAdmin(currentUser, team.getOrganization().getId());
        return repository.save(team);
    }

    public List<Team> findByOrganization(Long organizationId, User currentUser) {
        authorizationService.requireMembership(currentUser, organizationId);
        return repository.findByOrganizationId(organizationId);
    }
}
