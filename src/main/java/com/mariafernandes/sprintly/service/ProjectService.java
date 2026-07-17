package com.mariafernandes.sprintly.service;

import com.mariafernandes.sprintly.domain.Project;
import com.mariafernandes.sprintly.domain.Team;
import com.mariafernandes.sprintly.domain.User;
import com.mariafernandes.sprintly.repository.ProjectRepository;
import com.mariafernandes.sprintly.repository.TeamRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProjectService {

    private final ProjectRepository repository;
    private final TeamRepository teamRepository;
    private final AuthorizationService authorizationService;

    public ProjectService(ProjectRepository repository,
                          TeamRepository teamRepository,
                          AuthorizationService authorizationService) {
        this.repository = repository;
        this.teamRepository = teamRepository;
        this.authorizationService = authorizationService;
    }

    public Project create(Project project, User currentUser) {
        if (project.getTeam() == null || project.getTeam().getId() == null) {
            throw new IllegalArgumentException("team.id é obrigatório");
        }

        Team team = teamRepository.findById(project.getTeam().getId())
                .orElseThrow(() -> new IllegalArgumentException("Team não encontrado"));

        authorizationService.requireAdmin(currentUser, team.getOrganization().getId());
        project.setTeam(team);
        return repository.save(project);
    }

    public List<Project> findByTeam(Long teamId, User currentUser) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("Team não encontrado"));
        authorizationService.requireMembership(currentUser, team.getOrganization().getId());
        return repository.findByTeamId(teamId);
    }
}
