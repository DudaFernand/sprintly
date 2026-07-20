package com.mariafernandes.sprintly.controller;

import com.mariafernandes.sprintly.domain.Notification;
import com.mariafernandes.sprintly.domain.User;
import com.mariafernandes.sprintly.repository.NotificationRepository;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationRepository repository;

    public NotificationController(NotificationRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<Notification> findMine(@AuthenticationPrincipal User currentUser) {
        return repository.findByRecipientIdOrderByCreatedAtDesc(currentUser.getId());
    }
}