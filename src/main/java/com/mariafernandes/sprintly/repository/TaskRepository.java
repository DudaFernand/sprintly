package com.mariafernandes.sprintly.repository;

import com.mariafernandes.sprintly.domain.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByBoardId(Long boardId);
    long countByStatusId(Long statusId);
}