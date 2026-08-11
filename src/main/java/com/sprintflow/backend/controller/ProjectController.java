package com.sprintflow.backend.controller;

import com.sprintflow.backend.dto.project.*;
import com.sprintflow.backend.service.ProjectService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@SecurityRequirement(name = "bearerAuth")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping("/workspaces/{workspaceId}/projects")
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectResponse createProject(
            @PathVariable UUID workspaceId,
            @Valid @RequestBody CreateProjectRequest request,
            Authentication authentication) {

        return projectService.createProject(
                workspaceId,
                request,
                authentication
        );
    }

    @GetMapping("/workspaces/{workspaceId}/projects")
    public List<ProjectResponse> getProjects(
            @PathVariable UUID workspaceId,
            Authentication authentication) {

        return projectService.getProjects(
                workspaceId,
                authentication
        );
    }

    @GetMapping("/projects/{projectId}")
    public ProjectResponse getProject(
            @PathVariable UUID projectId,
            Authentication authentication) {

        return projectService.getProject(
                projectId,
                authentication
        );
    }

    @PutMapping("/projects/{projectId}")
    public ProjectResponse updateProject(
            @PathVariable UUID projectId,
            @Valid @RequestBody UpdateProjectRequest request,
            Authentication authentication) {

        return projectService.updateProject(
                projectId,
                request,
                authentication
        );
    }

    @DeleteMapping("/projects/{projectId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProject(
            @PathVariable UUID projectId,
            Authentication authentication) {

        projectService.deleteProject(
                projectId,
                authentication
        );
    }

    @PostMapping("/projects/{projectId}/members")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void addProjectMember(
            @PathVariable UUID projectId,
            @Valid @RequestBody AddProjectMemberRequest request,
            Authentication authentication) {

        projectService.addProjectMember(
                projectId,
                request,
                authentication
        );
    }

    @GetMapping("/projects/{projectId}/members")
    public List<ProjectMemberResponse> getProjectMembers(
            @PathVariable UUID projectId,
            Authentication authentication) {

        return projectService.getProjectMembers(
                projectId,
                authentication
        );
    }

    @PutMapping("/projects/{projectId}/members/{userId}")
    public ProjectMemberResponse updateProjectMemberRole(
            @PathVariable UUID projectId,
            @PathVariable UUID userId,
            @Valid @RequestBody UpdateProjectMemberRequest request,
            Authentication authentication) {

        return projectService.updateProjectMemberRole(
                projectId,
                userId,
                request,
                authentication
        );
    }

    @DeleteMapping("/projects/{projectId}/members/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeProjectMember(
            @PathVariable UUID projectId,
            @PathVariable UUID userId,
            Authentication authentication) {

        projectService.removeProjectMember(
                projectId,
                userId,
                authentication
        );
    }
}