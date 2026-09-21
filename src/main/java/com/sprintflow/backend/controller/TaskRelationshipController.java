package com.sprintflow.backend.controller;

import org.springframework.web.bind.annotation.RestController;
import com.sprintflow.backend.dto.task.TaskRelationshipRequest;
import com.sprintflow.backend.dto.task.TaskRelationshipResponse;
import com.sprintflow.backend.service.TaskRelationshipService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/task-relationships")
@SecurityRequirement(name = "bearerAuth")
public class TaskRelationshipController {

    private final TaskRelationshipService taskRelationshipService;

    public TaskRelationshipController(
            TaskRelationshipService taskRelationshipService) {
        this.taskRelationshipService = taskRelationshipService;
    }

    @PostMapping
    public ResponseEntity<TaskRelationshipResponse> createRelationship(
            @RequestBody TaskRelationshipRequest request,
            Authentication authentication) {

        return ResponseEntity.status(201).body(
                taskRelationshipService.createRelationship(
                        request,
                        authentication
                )
        );
    }

    @GetMapping("/task/{taskId}")
    public ResponseEntity<List<TaskRelationshipResponse>> getRelationships(
            @PathVariable UUID taskId,
            Authentication authentication) {

        return ResponseEntity.ok(
                taskRelationshipService.getRelationshipsForTask(
                        taskId,
                        authentication
                )
        );
    }

    @DeleteMapping("/{relationshipId}")
    public ResponseEntity<Void> deleteRelationship(
            @PathVariable UUID relationshipId,
            Authentication authentication) {

        taskRelationshipService.deleteRelationship(
                relationshipId,
                authentication
        );

        return ResponseEntity.noContent().build();
    }
}