package com.sprintflow.backend.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FcmTokenRequestDto {

    @NotBlank
    private String fcmToken;
}
