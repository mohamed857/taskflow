package com.taskflow.repository;

import com.taskflow.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task,Long> {
    List<Task> findByStatusNotAndDueDateBefore(Task.TaskStatus status, LocalDateTime now);

    @Modifying
    @Query("UPDATE Task t set t.status = 'OVERDUE' WHERE t.status = 'PENDING' and t.dueDate < :now")
    int markOverDueTask(@Param("now") LocalDateTime now);
}
