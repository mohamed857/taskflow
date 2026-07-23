package com.taskflow.controller;

import com.taskflow.config.UserPrincipal;
import com.taskflow.dto.TaskRequest;
import com.taskflow.dto.TaskResponse;
import com.taskflow.dto.TaskUpdateRequest;
import com.taskflow.entity.Task;
import com.taskflow.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @Operation(summary = "Create a new task", description = "Assigns a new task to the currently logged-in user.")
    public ResponseEntity<TaskResponse> createTask(@Valid @RequestBody TaskRequest request, @AuthenticationPrincipal UserPrincipal principal){
            Long userId = principal.getId();

            TaskResponse response = taskService.createTask(request,userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/all")
    @Operation(summary = "Get all tasks (only Admin & Manager )")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<TaskResponse>> getAllTasks(){
        return ResponseEntity.status(HttpStatus.OK).body(taskService.getAllTasks());
    }

    @GetMapping
    @Operation(summary = "Get tasks. Use ?assigned=true to get tasks assigned to you")
    public ResponseEntity<List<TaskResponse>> getTasks(
            @RequestParam(required = false, defaultValue = "false") boolean assigned, // ده الـ Query Parameter
            @AuthenticationPrincipal UserPrincipal principal) {

        if (assigned) {
            return ResponseEntity.ok(taskService.getAssignedTasks(principal.getId()));
        }
        return ResponseEntity.ok(taskService.getReportedTasks(principal.getId()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a specific task by ID")
    public ResponseEntity<TaskResponse> getTaskById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(taskService.getTaskById(id, principal.getId()));
    }


    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Update a task fully or partially")
    public ResponseEntity<TaskResponse> updateTask(
            @PathVariable Long id,
            @RequestBody TaskUpdateRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(taskService.updateTask(id, principal.getId(), request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Delete a task")
    public ResponseEntity<Void> deleteTask(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        taskService.deleteTask(id, principal.getId());
        return ResponseEntity.noContent().build(); // 204 No Content
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "update task status")
    public ResponseEntity<TaskResponse> updateTaskStatus(
            @PathVariable Long id,
            @RequestBody Map<String,String> body,
            @AuthenticationPrincipal UserPrincipal principal){

        Task.TaskStatus status= Task.TaskStatus.valueOf(body.get("status"));
        return  ResponseEntity.ok( taskService.updateTaskStatus(id,principal.getId(),status));
    }

}
