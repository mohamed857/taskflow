package com.taskflow.repository;

import com.taskflow.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task,Long> {
    List<Task> findByStatusNotAndDueDateBefore(Task.TaskStatus status, LocalDateTime now);

    List<Task> findByReporterIdOrderByCreatedAtDesc(Long reporterId);
    List<Task> findByAssigneeIdOrderByCreatedAtDesc(Long assigneeId);

    // للحذف والتعديل الكامل - Reporter بس
    Optional<Task> findByIdAndReporterId(Long taskId, Long reporterId);

    @Modifying
    @Query("UPDATE Task t set t.status = 'OVERDUE' WHERE t.status = 'PENDING' and t.dueDate < :now")
    int markOverDueTask(@Param("now") LocalDateTime now);

    // للعرض وتحديث الـ Status - أي حد من الاتنين
    @Query("SELECT t FROM Task t WHERE t.id = :taskId AND (t.reporter.id = :userId or t.assignee.id = :userId)")
    Optional<Task> findByIdAndReporterIdOrAssigneeId(@Param("taskId") Long taskId, @Param("userId") Long userId);

}
