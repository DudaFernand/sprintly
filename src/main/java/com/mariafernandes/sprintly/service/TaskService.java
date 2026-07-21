package com.mariafernandes.sprintly.service;

import com.mariafernandes.sprintly.domain.*;
import com.mariafernandes.sprintly.dto.CreateTaskRequest;
import com.mariafernandes.sprintly.dto.UpdateTaskRequest;
import com.mariafernandes.sprintly.repository.*;
import org.springframework.stereotype.Service;


import java.util.List;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final BoardRepository boardRepository;
    private final ProjectRepository projectRepository;
    private final StatusRepository statusRepository;
    private final EpicRepository epicRepository;
    private final UserRepository userRepository;
    private final LabelRepository labelRepository;
    private final AuthorizationService authorizationService;
    private final SprintRepository sprintRepository;
    private final AuditService auditService;
    private final NotificationPublisher notificationPublisher;

    public TaskService(TaskRepository taskRepository, BoardRepository boardRepository,
            ProjectRepository projectRepository,
            StatusRepository statusRepository, EpicRepository epicRepository,
            UserRepository userRepository, LabelRepository labelRepository,
            AuthorizationService authorizationService, SprintRepository sprintRepository,
            AuditService auditService, NotificationPublisher notificationPublisher) {
        this.taskRepository = taskRepository;
        this.boardRepository = boardRepository;
        this.projectRepository = projectRepository;
        this.statusRepository = statusRepository;
        this.epicRepository = epicRepository;
        this.userRepository = userRepository;
        this.labelRepository = labelRepository;
        this.authorizationService = authorizationService;
        this.sprintRepository = sprintRepository;
        this.auditService = auditService;
        this.notificationPublisher = notificationPublisher;
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

    public List<Task> findByBoard(Long boardId, User currentUser) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("Board não encontrado"));
        Long organizationId = board.getProject().getTeam().getOrganization().getId();
        authorizationService.requireMembership(currentUser, organizationId);
        return taskRepository.findByBoardId(boardId);
    }

    public List<Task> findBySprint(Long sprintId, User currentUser) {
        Sprint sprint = sprintRepository.findById(sprintId)
                .orElseThrow(() -> new IllegalArgumentException("Sprint não encontrada"));
        Long organizationId = sprint.getProject().getTeam().getOrganization().getId();
        authorizationService.requireMembership(currentUser, organizationId);
        return taskRepository.findBySprintId(sprintId);
    }

    public List<Task> findByProject(Long projectId, User currentUser) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project não encontrado"));
        Long organizationId = project.getTeam().getOrganization().getId();
        authorizationService.requireMembership(currentUser, organizationId);
        return taskRepository.findByProjectId(projectId);
    }

    public Task update(Long taskId, UpdateTaskRequest request, User currentUser) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Tarefa não encontrada"));

        Long organizationId = task.getBoard().getProject().getTeam().getOrganization().getId();
        authorizationService.requireMembership(currentUser, organizationId);

        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setType(request.type());
        task.setPriority(request.priority());
        task.setStoryPoints(request.storyPoints());

        if (request.statusId() != null) {
            Status status = statusRepository.findById(request.statusId())
                    .orElseThrow(() -> new IllegalArgumentException("Status não encontrado"));
            task.setStatus(status);
        }

        if (request.assigneeId() != null) {
            User assignee = userRepository.findById(request.assigneeId())
                    .orElseThrow(() -> new IllegalArgumentException("Usuário responsável não encontrado"));
            task.setAssignee(assignee);
        } else {
            task.setAssignee(null);
        }

        Task saved = taskRepository.save(task);
        auditService.log(currentUser, "Task", taskId, "UPDATE",
                "title/description/type/priority/status/assignee atualizados");
        return saved;
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

        if (task.getAssignee() != null) {
            notificationPublisher.publish(
                task.getAssignee().getId(),
                "status_change",
                "A tarefa \"" + task.getTitle() + "\" mudou para " + newStatus.getName()
            );
        }

        String changes = String.format(
            "status: %s(id=%d) -> %s(id=%d) | storyPoints: %s | sprintId: %s",
            oldStatus.getName(), oldStatus.getId(),
            newStatus.getName(), newStatus.getId(),
            task.getStoryPoints(), task.getSprint() != null ? task.getSprint().getId() : "null"
        );

        auditService.log(currentUser, "Task", taskId, "STATUS_CHANGE", changes);
    
        return saved;
    }

    public Task addLabel(Long taskId, Long labelId, User currentUser) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Tarefa não encontrada"));
        Long organizationId = task.getBoard().getProject().getTeam().getOrganization().getId();
        authorizationService.requireMembership(currentUser, organizationId);

        Label label = labelRepository.findById(labelId)
                .orElseThrow(() -> new IllegalArgumentException("Label não encontrada"));
        task.getLabels().add(label);
        return taskRepository.save(task);
    }

    public Task removeLabel(Long taskId, Long labelId, User currentUser) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Tarefa não encontrada"));
        Long organizationId = task.getBoard().getProject().getTeam().getOrganization().getId();
        authorizationService.requireMembership(currentUser, organizationId);

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