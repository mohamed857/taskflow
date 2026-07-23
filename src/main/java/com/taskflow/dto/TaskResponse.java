package com.taskflow.dto;

import com.taskflow.entity.Task;

import java.time.LocalDateTime;

public record TaskResponse(
        Long id,
        String title,
        String description,
        LocalDateTime dueTime,
        Task.TaskStatus status,
        String assignedTo
) {
    public static TaskResponse fromEntity(Task task){
        String assigneeName= (task.getAssignee() != null)? task.getAssignee().getUsername(): null;
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getDueDate(),
                task.getStatus(),
                assigneeName
                );
    }
}
