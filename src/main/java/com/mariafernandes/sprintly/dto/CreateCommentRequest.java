package com.mariafernandes.sprintly.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateCommentRequest(
    @NotBlank String content,
    @NotNull Long taskId
) {}