package com.taskflow.controller;

import com.taskflow.config.UserPrincipal;
import com.taskflow.dto.TaskRequest;
import com.taskflow.dto.TaskResponse;
import com.taskflow.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {
    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    public ResponseEntity<TaskResponse> createTask(@Valid @RequestBody TaskRequest request, @AuthenticationPrincipal UserPrincipal principal){
            Long userId = principal.getId();

            TaskResponse response = taskService.createTask(request,userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

}
