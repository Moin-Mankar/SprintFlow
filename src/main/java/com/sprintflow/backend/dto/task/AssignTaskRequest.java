package com.sprintflow.backend.dto.task;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class AssignTaskRequest {

    @NotNull
    private UUID assigneeId;
}