package com.taskflow.service;

import com.taskflow.annotation.LogExecutionTime;
import com.taskflow.dto.TaskRequest;
import com.taskflow.dto.TaskResponse;
import com.taskflow.entity.Task;
import com.taskflow.entity.User;
import com.taskflow.repository.TaskRepository;
import com.taskflow.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class TaskService {
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public TaskService(TaskRepository taskRepository, UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    @LogExecutionTime
    public TaskResponse createTask(TaskRequest request, Long userId){
        // 1. نجيب الـ User من الداتا بيز عشان نربطه بالمهمة (JPA محتاج الـ Entity)
        User user = userRepository.findById(userId)
                .orElseThrow(()->new RuntimeException("User not found"));

        Task task = Task.builder()
                .title(request.title())
                .description(request.description())
                .dueDate(request.dueDate())
                .user(user)
                .status(Task.TaskStatus.PENDING)
                .build();
        Task savedTask = taskRepository.save(task);
        return TaskResponse.fromEntity(savedTask);
    }
}
