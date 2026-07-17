package com.mariafernandes.sprintly.service;

import com.mariafernandes.sprintly.domain.Board;
import com.mariafernandes.sprintly.domain.Status;
import com.mariafernandes.sprintly.domain.User;
import com.mariafernandes.sprintly.repository.BoardRepository;
import com.mariafernandes.sprintly.repository.StatusRepository;
import com.mariafernandes.sprintly.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StatusService {

    private final StatusRepository statusRepository;
    private final BoardRepository boardRepository;
    private final TaskRepository taskRepository;
    private final AuthorizationService authorizationService;

    public StatusService(StatusRepository statusRepository,
                         BoardRepository boardRepository,
                         TaskRepository taskRepository,
                         AuthorizationService authorizationService) {
        this.statusRepository = statusRepository;
        this.boardRepository = boardRepository;
        this.taskRepository = taskRepository;
        this.authorizationService = authorizationService;
    }

    public Status create(Status status, User currentUser) {
        if (status.getBoard() == null || status.getBoard().getId() == null) {
            throw new IllegalArgumentException("board.id é obrigatório");
        }

        Board board = boardRepository.findById(status.getBoard().getId())
                .orElseThrow(() -> new IllegalArgumentException("Board não encontrado"));

        Long organizationId = board.getProject().getTeam().getOrganization().getId();
        authorizationService.requireAdmin(currentUser, organizationId);

        status.setBoard(board);
        return statusRepository.save(status);
    }

    public List<Status> findByBoard(Long boardId, User currentUser) {
        Board board = boardRepository.findById(boardId)
            .orElseThrow(() -> new IllegalArgumentException("Board não encontrado"));
    
        Long organizationId = board.getProject().getTeam().getOrganization().getId();
        authorizationService.requireMembership(currentUser, organizationId);
    
        return statusRepository.findByBoardIdOrderBySortOrder(boardId);
    }

    public void delete(Long statusId, Long boardId, User currentUser) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("Board não encontrado"));

        Long organizationId = board.getProject().getTeam().getOrganization().getId();
        authorizationService.requireAdmin(currentUser, organizationId);

        Status status = statusRepository.findById(statusId)
                .orElseThrow(() -> new IllegalArgumentException("Status não encontrado"));

        if (!status.getBoard().getId().equals(boardId)) {
            throw new IllegalArgumentException("Status não pertence a este board");
        }

        long tasksUsingStatus = taskRepository.countByStatusId(statusId);
        if (tasksUsingStatus > 0) {
            throw new IllegalStateException(
                    "Não é possível remover um status com " + tasksUsingStatus + " tarefa(s) vinculada(s)");
        }
        statusRepository.delete(status);
    }

    public Status markAsDone(Long statusId, Boolean done, User currentUser) {
        Status status = statusRepository.findById(statusId)
            .orElseThrow(() -> new IllegalArgumentException("Status não encontrado"));
    
        Long organizationId = status.getBoard().getProject().getTeam().getOrganization().getId();
        authorizationService.requireAdmin(currentUser, organizationId);
    
        status.setDone(done);
        return statusRepository.save(status);
    }
}
