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

    public TaskService(TaskRepository taskRepository, BoardRepository boardRepository,
            StatusRepository statusRepository, EpicRepository epicRepository,
            UserRepository userRepository, LabelRepository labelRepository, 
            AuthorizationService authorizationService) {
        this.taskRepository = taskRepository;
        this.boardRepository = boardRepository;
        this.statusRepository = statusRepository;
        this.epicRepository = epicRepository;
        this.userRepository = userRepository;
        this.labelRepository = labelRepository;
        this.authorizationService = authorizationService;
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

    public Task updateStatus(Long taskId, Long newStatusId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Tarefa não encontrada"));
        Status newStatus = statusRepository.findById(newStatusId)
                .orElseThrow(() -> new IllegalArgumentException("Status não encontrado"));
        task.setStatus(newStatus);
        return taskRepository.save(task);
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
}