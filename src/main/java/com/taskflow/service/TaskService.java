package com.taskflow.service;

import com.taskflow.annotation.LogExecutionTime;
import com.taskflow.dto.TaskRequest;
import com.taskflow.dto.TaskResponse;
import com.taskflow.dto.TaskUpdateRequest;
import com.taskflow.entity.Task;
import com.taskflow.entity.User;
import com.taskflow.exception.ResourceNotFoundException;
import com.taskflow.repository.TaskRepository;
import com.taskflow.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

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
        User reporter = userRepository.findById(userId)
                .orElseThrow(()->new ResourceNotFoundException("User not found"));

        User assignee = (request.assigneeId() != null)
                ? userRepository.findById(request.assigneeId())
                .orElseThrow(() -> new ResourceNotFoundException("Assignee user not found"))
                : reporter;

        Task task = Task.builder()
                .title(request.title())
                .description(request.description())
                .dueDate(request.dueDate())
                .reporter(reporter)
                .assignee(assignee)
                .status(Task.TaskStatus.PENDING)
                .build();

        return TaskResponse.fromEntity(taskRepository.save(task));
    }
    @LogExecutionTime
    public List<TaskResponse> getAllTasks(){
        return taskRepository.findAll().stream().map(TaskResponse::fromEntity).collect(Collectors.toList());
    }
    @LogExecutionTime
    public List<TaskResponse> getReportedTasks(Long userId){
        return taskRepository.findByReporterIdOrderByCreatedAtDesc(userId).stream().map(TaskResponse::fromEntity).collect(Collectors.toList());
    }

    @LogExecutionTime
    public List<TaskResponse> getAssignedTasks(Long userId){
        return taskRepository.findByAssigneeIdOrderByCreatedAtDesc(userId).stream().map(TaskResponse::fromEntity).collect(Collectors.toList());
    }

    @LogExecutionTime
    public TaskResponse getTaskById(Long taskId, Long userId){
        Task task = taskRepository.findByIdAndReporterIdOrAssigneeId(taskId,userId).orElseThrow(
                ()->new ResourceNotFoundException("Task not found or you don't have permission")
        );
        return TaskResponse.fromEntity(task);
    }
    @LogExecutionTime
    public void deleteTask(Long taskId, Long userId){
        Task task = taskRepository.findByIdAndReporterId(taskId,userId).orElseThrow(
                ()-> new ResourceNotFoundException("Task not found or you don't have permission"));
         taskRepository.deleteById(taskId);
    }

    @LogExecutionTime
    @Transactional
    public TaskResponse updateTask(Long taskId, Long userId, TaskUpdateRequest request){
        Task task =taskRepository.findByIdAndReporterIdOrAssigneeId(taskId,userId).orElseThrow(
                ()-> new ResourceNotFoundException("Task not found or you don't have permission")
        );
        if (request.title() != null && !request.title().isBlank()) task.setTitle(request.title());
        if (request.description() != null) task.setDescription(request.description());
        if (request.dueDate() != null) task.setDueDate(request.dueDate());
        if (request.status() != null) task.setStatus(request.status());
        if (request.assigneeId() != null) {
            User assignee = userRepository.findById(request.assigneeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Assignee user not found"));
            task.setAssignee(assignee);
        }
        return TaskResponse.fromEntity(taskRepository.save(task));

    }


    @LogExecutionTime
    @Transactional
    public TaskResponse updateTaskStatus(Long taskId, Long userId, Task.TaskStatus status) {
        Task task = taskRepository.findByIdAndReporterIdOrAssigneeId(taskId,userId).orElseThrow(
                ()-> new ResourceNotFoundException("Task not found or you don't have permission")
        );
        task.setStatus(status);
        return TaskResponse.fromEntity(taskRepository.save(task));
    }
}
