package com.sprintflow.backend.dto.project;

import com.sprintflow.backend.enums.ProjectRole;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class ProjectMemberResponse {

    private UUID userId;
    private String name;
    private String email;
    private ProjectRole projectRole;
}