package com.sprintflow.backend.dto.auth;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class RegisterResponse {

    private UUID userId;
    private String name;
    private String email;
}
