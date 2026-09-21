package com.sprintflow.backend.controller;

import com.sprintflow.backend.dto.notification.NotificationResponseDto;
import com.sprintflow.backend.entity.Notification;
import com.sprintflow.backend.entity.User;
import com.sprintflow.backend.service.NotificationService;
import com.sprintflow.backend.service.UserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@SecurityRequirement(name = "bearerAuth")
public class NotificationController {

    private final UserService userService;
    private final NotificationService notificationService;

    public NotificationController(UserService userService, NotificationService notificationService) {
        this.userService = userService;
        this.notificationService = notificationService;
    }

    @GetMapping
    public List<NotificationResponseDto> getNotifications(Authentication authentication){
        User user = userService.getUserByEmail(authentication.getName());

        return notificationService.getNotificationsForUser(user);
    }

    @PatchMapping("/{notificationId}/read")
    public void markAsRead(@PathVariable UUID notificationId, Authentication authentication){

        User user = userService.getUserByEmail(authentication.getName());

        notificationService.markAsRead(notificationId, user);
    }
}
