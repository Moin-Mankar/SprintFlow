package com.sprintflow.backend.dto.notification;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDateTime;
import java.util.UUID;

@Setter
@Getter
public class NotificationResponseDto {

    private UUID id;
    private String message;
    private boolean read;
    private LocalDateTime createdAt;
}
