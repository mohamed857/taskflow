package com.taskflow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record TaskRequest(
        @NotBlank(message = "Title is required")
        String title,
        String description,
        @NotNull(message = "Due date is required")
        LocalDateTime dueDate,
        Long assigneeId
) {
}
