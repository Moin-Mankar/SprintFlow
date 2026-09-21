package com.sprintflow.backend.dto.task;

import com.sprintflow.backend.enums.TaskRelationshipType;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class TaskRelationshipResponse {

    private UUID id;

    private UUID sourceTaskId;

    private String sourceTaskTitle;

    private UUID targetTaskId;

    private String targetTaskTitle;

    private TaskRelationshipType type;

    private LocalDateTime createdAt;
}
