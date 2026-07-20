package com.taskflow.controller;

import com.taskflow.config.UserPrincipal;
import com.taskflow.dto.TaskRequest;
import com.taskflow.dto.TaskResponse;
import com.taskflow.dto.TaskUpdateRequest;
import com.taskflow.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@Tag(name = "Tasks", description = "Endpoints for creating and managing user tasks")
@SecurityRequirement(name = "bearerAuth")
public class TaskController {
    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    @Operation(summary = "Create a new task", description = "Assigns a new task to the currently logged-in user.")
    public ResponseEntity<TaskResponse> createTask(@Valid @RequestBody TaskRequest request, @AuthenticationPrincipal UserPrincipal principal){
            Long userId = principal.getId();

            TaskResponse response = taskService.createTask(request,userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/all")
    @Operation(summary = "Get all tasks")
    public ResponseEntity<List<TaskResponse>> getAllTasks(){
        return ResponseEntity.status(HttpStatus.OK).body(taskService.getAllTasks());
    }

    @GetMapping
    @Operation(summary = "Get all tasks for the logged-in user")
    public ResponseEntity<List<TaskResponse>> getMyTasks(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(taskService.getUserTasks(principal.getId()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a specific task by ID")
    public ResponseEntity<TaskResponse> getTaskById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(taskService.getTaskById(id, principal.getId()));
    }


    @PutMapping("/{id}")
    @Operation(summary = "Update a task fully or partially")
    public ResponseEntity<TaskResponse> updateTask(
            @PathVariable Long id,
            @RequestBody TaskUpdateRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(taskService.updateTask(id, principal.getId(), request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a task")
    public ResponseEntity<Void> deleteTask(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        taskService.deleteTask(id, principal.getId());
        return ResponseEntity.noContent().build(); // 204 No Content
    }


}
