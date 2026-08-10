package com.sprintflow.backend.dto.workspace;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateInvitationRequest {

    @NotBlank
    @Email
    private String email;
}