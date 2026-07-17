package com.mariafernandes.sprintly.service;

import com.mariafernandes.sprintly.domain.*;
import com.mariafernandes.sprintly.dto.CreateTaskRequest;
import com.mariafernandes.sprintly.repository.*;
import org.springframework.stereotype.Service;


import java.util.List;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final BoardRepository boardRepository;
    private final StatusRepository statusRepository;
    private final EpicRepository epicRepository;
    private final UserRepository userRepository;
    private final LabelRepository labelRepository;
    private final AuthorizationService authorizationService;
    private final SprintRepository sprintRepository;
    private final AuditService auditService;

    public TaskService(TaskRepository taskRepository, BoardRepository boardRepository,
            StatusRepository statusRepository, EpicRepository epicRepository,
            UserRepository userRepository, LabelRepository labelRepository, 
            AuthorizationService authorizationService, SprintRepository sprintRepository,
            AuditService auditService) {
        this.taskRepository = taskRepository;
        this.boardRepository = boardRepository;
        this.statusRepository = statusRepository;
        this.epicRepository = epicRepository;
        this.userRepository = userRepository;
        this.labelRepository = labelRepository;
        this.authorizationService = authorizationService;
        this.sprintRepository = sprintRepository;
        this.auditService = auditService;
    }

    public Task create(CreateTaskRequest request, User reporter) {
        Board board = boardRepository.findById(request.boardId())
                .orElseThrow(() -> new IllegalArgumentException("Board não encontrado"));

        Long organizationId = board.getProject().getTeam().getOrganization().getId();
        authorizationService.requireMembership(reporter, organizationId); // membro comum já pode criar tarefa

        Status status = statusRepository.findById(request.statusId())
                .orElseThrow(() -> new IllegalArgumentException("Status não encontrado"));

        Task task = new Task(request.title(), request.type(), request.priority(), board, status, reporter);
        task.setDescription(request.description());
        task.setStoryPoints(request.storyPoints());

        if (request.epicId() != null) {
            Epic epic = epicRepository.findById(request.epicId())
                    .orElseThrow(() -> new IllegalArgumentException("Epic não encontrado"));
            task.setEpic(epic);
        }

        if (request.parentTaskId() != null) {
            Task parent = taskRepository.findById(request.parentTaskId())
                    .orElseThrow(() -> new IllegalArgumentException("Tarefa pai não encontrada"));
            task.setParentTask(parent);
        }

        if (request.assigneeId() != null) {
            User assignee = userRepository.findById(request.assigneeId())
                    .orElseThrow(() -> new IllegalArgumentException("Usuário responsável não encontrado"));
            task.setAssignee(assignee);
        }

        return taskRepository.save(task);
    }

    public List<Task> findByBoard(Long boardId) {
        return taskRepository.findByBoardId(boardId);
    }

    public Task updateStatus(Long taskId, Long newStatusId, User currentUser) {
        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new IllegalArgumentException("Tarefa não encontrada"));
    
        Long organizationId = task.getBoard().getProject().getTeam().getOrganization().getId();
        authorizationService.requireMembership(currentUser, organizationId);
    
        Status oldStatus = task.getStatus();
        Status newStatus = statusRepository.findById(newStatusId)
            .orElseThrow(() -> new IllegalArgumentException("Status não encontrado"));
    
        task.setStatus(newStatus);
        Task saved = taskRepository.save(task);
    
        String changes = String.format(
            "status: %s -> %s | storyPoints: %s | sprintId: %s",
            oldStatus.getName(), newStatus.getName(),
            task.getStoryPoints(), task.getSprint() != null ? task.getSprint().getId() : "null"
        );
        auditService.log(currentUser, "Task", taskId, "STATUS_CHANGE", changes);
    
        return saved;
    }

    public Task addLabel(Long taskId, Long labelId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Tarefa não encontrada"));
        Label label = labelRepository.findById(labelId)
                .orElseThrow(() -> new IllegalArgumentException("Label não encontrada"));
        task.getLabels().add(label);
        return taskRepository.save(task);
    }

    public Task removeLabel(Long taskId, Long labelId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Tarefa não encontrada"));
        task.getLabels().removeIf(l -> l.getId().equals(labelId));
        return taskRepository.save(task);
    }

    public Task assignToSprint(Long taskId, Long sprintId, User currentUser) {
        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new IllegalArgumentException("Tarefa não encontrada"));
    
        Long organizationId = task.getBoard().getProject().getTeam().getOrganization().getId();
        authorizationService.requireMembership(currentUser, organizationId);
    
        if (sprintId == null) {
            task.setSprint(null); // volta pro backlog
        } else {
            Sprint sprint = sprintRepository.findById(sprintId)
                .orElseThrow(() -> new IllegalArgumentException("Sprint não encontrada"));
            task.setSprint(sprint);
        }
        return taskRepository.save(task);
    }
}