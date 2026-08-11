package com.sprintflow.backend.dto.comment;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class CommentResponse {

    private UUID id;
    private String content;
    private LocalDateTime createdAt;

    private UUID taskId;

    private UUID userId;
    private String userName;
}