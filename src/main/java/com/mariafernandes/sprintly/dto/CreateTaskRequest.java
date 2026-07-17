package com.mariafernandes.sprintly.dto;

import com.mariafernandes.sprintly.domain.Priority;
import com.mariafernandes.sprintly.domain.TaskType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateTaskRequest(
    @NotBlank String title,
    String description,
    @NotNull TaskType type,
    @NotNull Priority priority,
    @NotNull Long boardId,
    @NotNull Long statusId,
    Long epicId,
    Long parentTaskId,
    Long assigneeId,
    Integer storyPoints
) {}