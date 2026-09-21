package com.sprintflow.backend.service.websocket;


import com.sprintflow.backend.dto.websocket.ProjectEvent;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class WebSocketEventService {

    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketEventService(
            SimpMessagingTemplate messagingTemplate) {

        this.messagingTemplate = messagingTemplate;
    }

    public void sendProjectEvent(
            UUID projectId,
            ProjectEvent event) {

        messagingTemplate.convertAndSend(
                "/topic/projects/" + projectId,
                event
        );
    }
}