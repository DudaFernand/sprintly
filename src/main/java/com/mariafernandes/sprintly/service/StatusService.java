package com.mariafernandes.sprintly.service;

import com.mariafernandes.sprintly.domain.Status;
import com.mariafernandes.sprintly.repository.StatusRepository;
import com.mariafernandes.sprintly.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StatusService {

    private final StatusRepository statusRepository;
    private final TaskRepository taskRepository;

    public StatusService(StatusRepository statusRepository, TaskRepository taskRepository) {
        this.statusRepository = statusRepository;
        this.taskRepository = taskRepository;
    }

    public Status create(Status status) {
        return statusRepository.save(status);
    }

    public List<Status> findByBoard(Long boardId) {
        return statusRepository.findByBoardIdOrderByOrder(boardId);
    }

    public void delete(Long statusId) {
        long tasksUsingStatus = taskRepository.countByStatusId(statusId);
        if (tasksUsingStatus > 0) {
            throw new IllegalStateException(
                "Não é possível remover um status com " + tasksUsingStatus + " tarefa(s) vinculada(s)"
            );
        }
        statusRepository.deleteById(statusId);
    }
}