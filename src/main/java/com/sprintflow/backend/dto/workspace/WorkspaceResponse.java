package com.sprintflow.backend.dto.workspace;

import com.sprintflow.backend.enums.WorkspaceType;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class WorkspaceResponse {

    private UUID id;
    private String name;
    private String description;
    private WorkspaceType workspaceType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}