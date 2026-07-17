package com.mariafernandes.sprintly.repository;

import com.mariafernandes.sprintly.domain.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByEntityTypeAndEntityId(String entityType, Long entityId);

    List<AuditLog> findByEntityTypeAndActionAndCreatedAtBetween(
        String entityType, String action, LocalDateTime start, LocalDateTime end
    );
}