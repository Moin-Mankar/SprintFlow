package com.sprintflow.backend.controller;

import com.sprintflow.backend.dto.dashboard.ProjectDashboardResponse;
import com.sprintflow.backend.service.ProjectDashboardService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/projects/{projectId}/dashboard")
@SecurityRequirement(name = "bearerAuth")
public class ProjectDashboardController {

    private final ProjectDashboardService projectDashboardService;

    public ProjectDashboardController(
            ProjectDashboardService projectDashboardService) {
        this.projectDashboardService = projectDashboardService;
    }

    @GetMapping
    public ProjectDashboardResponse getDashboard(
            @PathVariable UUID projectId,
            Authentication authentication) {

        return projectDashboardService.getDashboard(
                projectId,
                authentication
        );
    }
}