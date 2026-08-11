package com.sprintflow.backend.controller;

import com.sprintflow.backend.dto.dashboard.DashboardResponse;
import com.sprintflow.backend.service.DashboardService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/workspaces/{workspaceId}/dashboard")
@SecurityRequirement(name = "bearerAuth")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    public DashboardResponse getDashboard(
            @PathVariable UUID workspaceId,
            Authentication authentication) {

        return dashboardService.getDashboard(
                workspaceId,
                authentication
        );
    }
}