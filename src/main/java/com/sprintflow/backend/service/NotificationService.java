package com.sprintflow.backend.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.sprintflow.backend.dto.notification.NotificationResponseDto;
import com.sprintflow.backend.entity.Notification;
import com.sprintflow.backend.entity.User;
import com.sprintflow.backend.repository.NotificationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public void sendNotification(User user, String message) {

        Notification notification = new Notification();

        notification.setUser(user);
        notification.setMessage(message);

        notificationRepository.save(notification);

        if (user.getFcmToken() == null || user.getFcmToken().isBlank()) {
            return;
        }

        Message fcmMessage = Message.builder()
                .setToken(user.getFcmToken())
                .setNotification(
                        com.google.firebase.messaging.Notification.builder()
                                .setTitle("SprintFlow")
                                .setBody(message)
                                .build()
                )
                .build();

        try {
            FirebaseMessaging.getInstance().send(fcmMessage);
        } catch (Exception e) {
            log.error(
                    "Failed to send FCM notification to user {}",
                    user.getId(),
                    e
            );
        }
    }

    public List<NotificationResponseDto> getNotificationsForUser(User user) {

        return notificationRepository
                .findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(notification -> {

                    NotificationResponseDto response =
                            new NotificationResponseDto();

                    response.setId(notification.getId());
                    response.setMessage(notification.getMessage());
                    response.setRead(notification.isRead());
                    response.setCreatedAt(notification.getCreatedAt());

                    return response;
                })
                .toList();
    }

    public void markAsRead(UUID notificationId, User user) {

        Notification notification = notificationRepository
                .findById(notificationId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Notification with id: "
                                        + notificationId
                                        + " not found"
                        )
                );

        if (!notification.getUser().getId().equals(user.getId())) {
            throw new RuntimeException(
                    "You are not allowed to access this notification"
            );
        }

        notification.setRead(true);
        notificationRepository.save(notification);
    }
}