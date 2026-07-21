package com.mariafernandes.sprintly.dto;

import com.mariafernandes.sprintly.domain.Priority;
import com.mariafernandes.sprintly.domain.TaskType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateTaskRequest(
    @NotBlank String title,
    String description,
    @NotNull TaskType type,
    @NotNull Priority priority,
    Integer storyPoints,
    Long assigneeId,
    Long statusId
) {}
