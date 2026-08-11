package com.sprintflow.backend.dto.board;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class BoardResponse {

    private UUID id;
    private String name;
    private int position;
    private UUID projectId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}