package com.sprintflow.backend.dto.websocket;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class ProjectEvent {

    private String type;

    private UUID projectId;

    private UUID taskId;

    private String message;
}