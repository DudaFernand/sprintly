package com.mariafernandes.sprintly.service;

import com.mariafernandes.sprintly.domain.AuditLog;
import com.mariafernandes.sprintly.domain.Task;
import com.mariafernandes.sprintly.domain.User;
import com.mariafernandes.sprintly.dto.AuditLogResponse;
import com.mariafernandes.sprintly.repository.AuditLogRepository;
import com.mariafernandes.sprintly.repository.TaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AuditService {

    private final AuditLogRepository repository;
    private final TaskRepository taskRepository;
    private final AuthorizationService authorizationService;

    public AuditService(AuditLogRepository repository,
                        TaskRepository taskRepository,
                        AuthorizationService authorizationService) {
        this.repository = repository;
        this.taskRepository = taskRepository;
        this.authorizationService = authorizationService;
    }

    public void log(User user, String entityType, Long entityId, String action, String changes) {
        AuditLog auditLog = new AuditLog(user, entityType, entityId, action, changes);
        repository.save(auditLog);
    }

    @Transactional(readOnly = true)
    public List<AuditLogResponse> findByEntity(String entityType, Long entityId, User currentUser) {
        Long organizationId = resolveOrganizationId(entityType, entityId);
        authorizationService.requireMembership(currentUser, organizationId);

        return repository.findByEntityTypeAndEntityId(entityType, entityId).stream()
            .map(AuditLogResponse::from)
            .toList();
    }

    private Long resolveOrganizationId(String entityType, Long entityId) {
        if ("Task".equalsIgnoreCase(entityType)) {
            Task task = taskRepository.findById(entityId)
                .orElseThrow(() -> new IllegalArgumentException("Task não encontrada"));
            return task.getBoard().getProject().getTeam().getOrganization().getId();
        }
        throw new IllegalArgumentException("Tipo de entidade não suportado para audit: " + entityType);
    }
}
