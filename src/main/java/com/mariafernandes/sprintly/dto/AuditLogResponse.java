package com.mariafernandes.sprintly.dto;

import com.mariafernandes.sprintly.domain.AuditLog;

import java.time.LocalDateTime;

public record AuditLogResponse(
    Long id,
    Long userId,
    String userEmail,
    String entityType,
    Long entityId,
    String action,
    String changes,
    LocalDateTime createdAt
) {
    public static AuditLogResponse from(AuditLog log) {
        return new AuditLogResponse(
            log.getId(),
            log.getUser().getId(),
            log.getUser().getEmail(),
            log.getEntityType(),
            log.getEntityId(),
            log.getAction(),
            log.getChanges(),
            log.getCreatedAt()
        );
    }
}
