package com.mariafernandes.sprintly.service;

import com.mariafernandes.sprintly.domain.Organization;
import com.mariafernandes.sprintly.domain.Team;
import com.mariafernandes.sprintly.domain.User;
import com.mariafernandes.sprintly.repository.OrganizationRepository;
import com.mariafernandes.sprintly.repository.TeamRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TeamService {

    private final TeamRepository repository;
    private final OrganizationRepository organizationRepository;
    private final AuthorizationService authorizationService;

    public TeamService(TeamRepository repository,
                       OrganizationRepository organizationRepository,
                       AuthorizationService authorizationService) {
        this.repository = repository;
        this.organizationRepository = organizationRepository;
        this.authorizationService = authorizationService;
    }

    public Team create(Team team, User currentUser) {
        if (team.getOrganization() == null || team.getOrganization().getId() == null) {
            throw new IllegalArgumentException("organization.id é obrigatório");
        }

        Organization organization = organizationRepository.findById(team.getOrganization().getId())
                .orElseThrow(() -> new IllegalArgumentException("Organization não encontrada"));

        authorizationService.requireAdmin(currentUser, organization.getId());
        team.setOrganization(organization);
        return repository.save(team);
    }

    public List<Team> findByOrganization(Long organizationId, User currentUser) {
        authorizationService.requireMembership(currentUser, organizationId);
        return repository.findByOrganizationId(organizationId);
    }
}
