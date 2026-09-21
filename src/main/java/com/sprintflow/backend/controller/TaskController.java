package com.sprintflow.backend.controller;

import com.sprintflow.backend.dto.task.*;
import com.sprintflow.backend.enums.TaskPriority;
import com.sprintflow.backend.enums.TaskStatus;
import com.sprintflow.backend.service.TaskService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/boards/{boardId}/tasks")
@SecurityRequirement(name = "bearerAuth")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TaskResponse createTask(
            @PathVariable UUID boardId,
            @Valid @RequestBody CreateTaskRequest request,
            Authentication authentication) {

        return taskService.createTask(
                boardId,
                request,
                authentication
        );
    }

    @GetMapping
    public List<TaskResponse> getTasks(
            @PathVariable UUID boardId,
            Authentication authentication) {

        return taskService.getTasks(
                boardId,
                authentication
        );
    }

    @GetMapping("/{taskId}")
    public TaskResponse getTask(
            @PathVariable UUID taskId,
            Authentication authentication) {

        return taskService.getTask(
                taskId,
                authentication
        );
    }

    @PutMapping("/{taskId}")
    public TaskResponse updateTask(
            @PathVariable UUID taskId,
            @Valid @RequestBody UpdateTaskRequest request,
            Authentication authentication) {

        return taskService.updateTask(
                taskId,
                request,
                authentication
        );
    }

    @DeleteMapping("/{taskId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTask(
            @PathVariable UUID taskId,
            Authentication authentication) {

        taskService.deleteTask(
                taskId,
                authentication
        );
    }

    @PutMapping("/{taskId}/assignee")
    public TaskResponse assignTask(
            @PathVariable UUID taskId,
            @Valid @RequestBody AssignTaskRequest request,
            Authentication authentication) {

        return taskService.assignTask(
                taskId,
                request,
                authentication
        );
    }

    @PutMapping("/{taskId}/move")
    public TaskResponse moveTask(
            @PathVariable UUID taskId,
            @Valid @RequestBody MoveTaskRequest request,
            Authentication authentication) {

        return taskService.moveTask(
                taskId,
                request,
                authentication
        );
    }

    @GetMapping("/search")
    public Page<TaskResponse> searchTasks(
            @PathVariable UUID boardId,
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) TaskPriority priority,
            @RequestParam(required = false) UUID assigneeId,
            @RequestParam(required = false) String title,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dueDateFrom,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dueDateTo,
            @PageableDefault(size = 10, page = 0,sort = "createdAt",
                    direction = Sort.Direction.DESC)
            Pageable pageable,
            Authentication authentication) {

        return taskService.searchTasks(
                boardId,
                status,
                priority,
                assigneeId,
                title,
                dueDateFrom,
                dueDateTo,
                pageable,
                authentication
        );
    }
}