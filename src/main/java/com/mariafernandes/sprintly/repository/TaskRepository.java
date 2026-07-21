package com.mariafernandes.sprintly.repository;

import com.mariafernandes.sprintly.domain.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByBoardId(Long boardId);
    long countByStatusId(Long statusId);
    List<Task> findBySprintId(Long sprintId);
    List<Task> findByBoardIdAndSprintIsNull(Long boardId);

    @Query("select t from Task t where t.board.project.id = :projectId")
    List<Task> findByProjectId(@Param("projectId") Long projectId);
}
