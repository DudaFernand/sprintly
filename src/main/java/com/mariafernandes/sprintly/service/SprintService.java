package com.mariafernandes.sprintly.service;

import com.mariafernandes.sprintly.domain.Project;
import com.mariafernandes.sprintly.domain.Sprint;
import com.mariafernandes.sprintly.domain.User;
import com.mariafernandes.sprintly.repository.ProjectRepository;
import com.mariafernandes.sprintly.repository.SprintRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SprintService {

    private final SprintRepository repository;
    private final ProjectRepository projectRepository;
    private final AuthorizationService authorizationService;

    public SprintService(SprintRepository repository,
                         ProjectRepository projectRepository,
                         AuthorizationService authorizationService) {
        this.repository = repository;
        this.projectRepository = projectRepository;
        this.authorizationService = authorizationService;
    }

    public Sprint create(Sprint sprint, User currentUser) {
        if (sprint.getProject() == null || sprint.getProject().getId() == null) {
            throw new IllegalArgumentException("project.id é obrigatório");
        }

        Project project = projectRepository.findById(sprint.getProject().getId())
                .orElseThrow(() -> new IllegalArgumentException("Project não encontrado"));

        authorizationService.requireAdmin(currentUser, project.getTeam().getOrganization().getId());
        sprint.setProject(project);
        return repository.save(sprint);
    }

    public List<Sprint> findByProject(Long projectId, User currentUser) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project não encontrado"));
        authorizationService.requireMembership(currentUser, project.getTeam().getOrganization().getId());
        return repository.findByProjectId(projectId);
    }
}
