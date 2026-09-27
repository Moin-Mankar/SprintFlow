package com.sprintflow.backend.controller;

import com.sprintflow.backend.dto.task.TaskActivityResponse;
import com.sprintflow.backend.entity.Task;
import com.sprintflow.backend.repository.ProjectMemberRepository;
import com.sprintflow.backend.repository.TaskRepository;
import com.sprintflow.backend.repository.UserRepository;
import com.sprintflow.backend.exception.ForbiddenException;
import com.sprintflow.backend.exception.ResourceNotFoundException;
import org.springframework.security.core.Authentication;
import com.sprintflow.backend.service.TaskActivityService;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
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
    private final UserRepository userRepository;
    private final ProjectMemberRepository projectMemberRepository;

    public TaskActivityController(
            TaskActivityService taskActivityService,
            TaskRepository taskRepository,
            UserRepository userRepository,
            ProjectMemberRepository projectMemberRepository) {
        this.taskActivityService = taskActivityService;
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.projectMemberRepository = projectMemberRepository;
    }

    @GetMapping("/{taskId}/activities")
    @Transactional(readOnly = true)
    public ResponseEntity<List<TaskActivityResponse>> getActivities(
            @PathVariable UUID taskId,
            Authentication authentication) {

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Task not found"));

        var user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Authenticated user not found"));

        var project = task.getBoard().getProject();

        projectMemberRepository.findByUserAndProject(user, project)
                .orElseThrow(() ->
                        new ForbiddenException("You are not a member of this project"));

        return ResponseEntity.ok(taskActivityService.getActivities(task));
    }
}
