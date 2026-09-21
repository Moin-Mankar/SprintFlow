package com.sprintflow.backend.controller;

import com.sprintflow.backend.dto.task.TaskActivityResponse;
import com.sprintflow.backend.entity.Task;
import com.sprintflow.backend.entity.TaskActivity;
import com.sprintflow.backend.repository.TaskActivityRepository;
import com.sprintflow.backend.repository.TaskRepository;
import com.sprintflow.backend.service.TaskActivityService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tasks")
public class TaskActivityController {


    private final TaskActivityService taskActivityService;
    private final TaskRepository taskRepository;

    public TaskActivityController(TaskActivityService taskActivityService, TaskRepository taskRepository) {
        this.taskActivityService = taskActivityService;
        this.taskRepository = taskRepository;
    }

    @GetMapping("/{taskId}/activities")
    public ResponseEntity<List<TaskActivityResponse>> getActivities (@PathVariable UUID taskId){
        Task task = taskRepository.findById(taskId).orElseThrow(()-> new RuntimeException("Task with id " + taskId + " not found"));

        return ResponseEntity.ok(taskActivityService.getActivities(task));
    }
}
