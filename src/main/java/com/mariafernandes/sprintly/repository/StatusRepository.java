package com.mariafernandes.sprintly.repository;

import com.mariafernandes.sprintly.domain.Status;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StatusRepository extends JpaRepository<Status, Long> {
    List<Status> findByBoardIdOrderByOrder(Long boardId);
}