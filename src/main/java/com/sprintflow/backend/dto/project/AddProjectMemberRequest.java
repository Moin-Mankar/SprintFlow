package com.sprintflow.backend.dto.project;

import com.sprintflow.backend.enums.ProjectRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddProjectMemberRequest {

    @NotBlank
    @Email
    private String email;

    @NotNull
    private ProjectRole projectRole;
}