package com.sprintflow.backend.dto.workspace;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class InvitationResponse {

    private UUID id;
    private String token;
    private String email;
    private Boolean used;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
    private UUID workspaceId;
    private UUID createdBy;
}