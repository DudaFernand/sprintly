package com.mariafernandes.sprintly.service;

import com.mariafernandes.sprintly.domain.AuditLog;
import com.mariafernandes.sprintly.domain.User;
import com.mariafernandes.sprintly.dto.AuditLogResponse;
import com.mariafernandes.sprintly.repository.AuditLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AuditService {

    private final AuditLogRepository repository;

    public AuditService(AuditLogRepository repository) {
        this.repository = repository;
    }

    public void log(User user, String entityType, Long entityId, String action, String changes) {
        AuditLog auditLog = new AuditLog(user, entityType, entityId, action, changes);
        repository.save(auditLog);
    }

    @Transactional(readOnly = true)
    public List<AuditLogResponse> findByEntity(String entityType, Long entityId) {
        return repository.findByEntityTypeAndEntityId(entityType, entityId).stream()
            .map(AuditLogResponse::from)
            .toList();
    }
}
