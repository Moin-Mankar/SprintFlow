package com.sprintflow.backend.controller;

import com.sprintflow.backend.dto.user.FcmTokenRequestDto;
import com.sprintflow.backend.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PutMapping("/fcm-token")
    public void setFcmToken(@Valid @RequestBody FcmTokenRequestDto request , Authentication authentication){
        userService.updateFcmToken(authentication.getName(), request.getFcmToken());
    }
}
