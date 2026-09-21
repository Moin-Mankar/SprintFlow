package com.sprintflow.backend.dto.dashboard;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class UserWorkloadResponse {

    private UUID userId;
    private String userName;
    private long assignedTasks;
}
