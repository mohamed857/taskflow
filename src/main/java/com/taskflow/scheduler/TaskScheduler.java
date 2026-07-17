package com.taskflow.scheduler;

import com.taskflow.repository.TaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class TaskScheduler {
    private static final Logger logger = LoggerFactory.getLogger(TaskScheduler.class);
    private final TaskRepository taskRepository;

    public TaskScheduler(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void checkForOverdueTask(){
        logger.info("Checking for overdue tasks...");
        int updatedCount = taskRepository.markOverDueTask(LocalDateTime.now());
        if (updatedCount > 0){
            logger.warn("Alert! {} tasks have been marked as OVERDUE.", updatedCount);
        }
    }
}
