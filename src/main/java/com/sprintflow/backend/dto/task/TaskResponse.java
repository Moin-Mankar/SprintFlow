package com.sprintflow.backend.dto.task;

import com.sprintflow.backend.enums.TaskPriority;
import com.sprintflow.backend.enums.TaskStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class TaskResponse {

    private UUID id;
    private String title;
    private String description;

    private TaskPriority taskPriority;
    private TaskStatus taskStatus;

    private LocalDateTime createdAt;
    private LocalDateTime dueDate;
    private LocalDateTime updatedAt;

    private UUID boardId;

    private UUID assigneeId;
    private String assigneeName;

    private UUID createdById;
    private String createdByName;
}
