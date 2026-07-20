package com.mariafernandes.sprintly.service;

import com.mariafernandes.sprintly.domain.Board;
import com.mariafernandes.sprintly.domain.Project;
import com.mariafernandes.sprintly.domain.User;
import com.mariafernandes.sprintly.repository.BoardRepository;
import com.mariafernandes.sprintly.repository.ProjectRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BoardService {

    private final BoardRepository repository;
    private final ProjectRepository projectRepository;
    private final AuthorizationService authorizationService;

    public BoardService(BoardRepository repository,
                        ProjectRepository projectRepository,
                        AuthorizationService authorizationService) {
        this.repository = repository;
        this.projectRepository = projectRepository;
        this.authorizationService = authorizationService;
    }

    public Board create(Board board, User currentUser) {
        if (board.getProject() == null || board.getProject().getId() == null) {
            throw new IllegalArgumentException("project.id é obrigatório");
        }

        Project project = projectRepository.findById(board.getProject().getId())
                .orElseThrow(() -> new IllegalArgumentException("Project não encontrado"));

        authorizationService.requireAdmin(currentUser, project.getTeam().getOrganization().getId());
        board.setProject(project);
        return repository.save(board);
    }

    public List<Board> findByProject(Long projectId, User currentUser) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project não encontrado"));
        authorizationService.requireMembership(currentUser, project.getTeam().getOrganization().getId());
        return repository.findByProjectId(projectId);
    }
}
