package com.taskflow.dto;

import com.taskflow.entity.Task;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record TaskUpdateRequest(
        String title,
        String description,
        LocalDateTime dueDate,
        Task.TaskStatus status,
        Long assigneeId
) {
}
