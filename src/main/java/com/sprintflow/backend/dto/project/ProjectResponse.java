package com.sprintflow.backend.dto.project;

import com.sprintflow.backend.enums.ProjectStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class ProjectResponse {

    private UUID id;
    private String name;
    private String description;
    private ProjectStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UUID workspaceId;
}