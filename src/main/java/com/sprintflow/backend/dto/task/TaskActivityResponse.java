package com.sprintflow.backend.dto.task;

import com.sprintflow.backend.enums.TaskActivityType;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class TaskActivityResponse {


    private UUID id;
    private TaskActivityType taskActivityType;
    private String description;
    private LocalDateTime createdAt;
    private UUID user_id;
    private String userName;
}
