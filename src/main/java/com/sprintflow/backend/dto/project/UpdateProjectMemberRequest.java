package com.sprintflow.backend.dto.project;

import com.sprintflow.backend.enums.ProjectRole;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateProjectMemberRequest {

    @NotNull
    private ProjectRole projectRole;
}