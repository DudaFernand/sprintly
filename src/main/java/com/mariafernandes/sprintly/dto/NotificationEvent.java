package com.mariafernandes.sprintly.dto;

public record NotificationEvent(
    Long recipientId,
    String type,
    String message
) {}
