package com.sprintflow.backend.dto.task;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class TaskRelationshipRequest {

    private UUID sourceTaskId;

    private UUID targetTaskId;
}
