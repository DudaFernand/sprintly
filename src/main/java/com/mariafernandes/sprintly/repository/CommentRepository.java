package com.mariafernandes.sprintly.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mariafernandes.sprintly.domain.Comment;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByTaskIdOrderByCreatedAt(Long taskId);
}
