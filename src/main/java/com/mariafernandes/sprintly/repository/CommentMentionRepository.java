package com.mariafernandes.sprintly.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mariafernandes.sprintly.domain.CommentMention;

public interface CommentMentionRepository extends JpaRepository<CommentMention, Long> {
    List<CommentMention> findByMentionedUserId(Long userId);
}
