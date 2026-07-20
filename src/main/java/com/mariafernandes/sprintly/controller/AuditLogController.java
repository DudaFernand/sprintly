package com.mariafernandes.sprintly.controller;

import com.mariafernandes.sprintly.dto.AuditLogResponse;
import com.mariafernandes.sprintly.service.AuditService;
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
    public List<AuditLogResponse> findByEntity(@RequestParam String entityType, @RequestParam Long entityId) {
        return service.findByEntity(entityType, entityId);
    }

    @GetMapping("/{entityType}/{entityId}")
    public List<AuditLogResponse> findByEntityPath(@PathVariable String entityType, @PathVariable Long entityId) {
        return service.findByEntity(entityType, entityId);
    }
}
