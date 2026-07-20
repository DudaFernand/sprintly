package com.mariafernandes.sprintly.controller;

import com.mariafernandes.sprintly.domain.User;
import com.mariafernandes.sprintly.dto.AuditLogResponse;
import com.mariafernandes.sprintly.service.AuditService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/audit-logs")
public class AuditLogController {

    private final AuditService service;

    public AuditLogController(AuditService service) {
        this.service = service;
    }

    @GetMapping
    public List<AuditLogResponse> findByEntity(@RequestParam String entityType,
                                               @RequestParam Long entityId,
                                               @AuthenticationPrincipal User currentUser) {
        return service.findByEntity(entityType, entityId, currentUser);
    }

    @GetMapping("/{entityType}/{entityId}")
    public List<AuditLogResponse> findByEntityPath(@PathVariable String entityType,
                                                   @PathVariable Long entityId,
                                                   @AuthenticationPrincipal User currentUser) {
        return service.findByEntity(entityType, entityId, currentUser);
    }
}
