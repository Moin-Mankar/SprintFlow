package com.sprintflow.backend.controller;

import com.sprintflow.backend.dto.workspace.CreateWorkspaceRequest;
import com.sprintflow.backend.dto.workspace.UpdateWorkspaceRequest;
import com.sprintflow.backend.dto.workspace.WorkspaceResponse;
import com.sprintflow.backend.entity.Workspace;
import com.sprintflow.backend.service.WorkspaceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/workspaces")
public class WorkspaceController {

    private final WorkspaceService workspaceService;

    public WorkspaceController(WorkspaceService workspaceService) {
        this.workspaceService = workspaceService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Workspace createWorkspace(
            @Valid @RequestBody CreateWorkspaceRequest request,
            Authentication authentication) {

        return workspaceService.createWorkspace(request, authentication);
    }

    @GetMapping
    public List<WorkspaceResponse> getMyWorkspaces(
            Authentication authentication) {

        return workspaceService.getMyWorkspaces(authentication);
    }

    @GetMapping("/{workspaceId}")
    public WorkspaceResponse getWorkspace(
            @PathVariable UUID workspaceId,
            Authentication authentication) {

        return workspaceService.getWorkspace(workspaceId, authentication);
    }

    @PutMapping("/{workspaceId}")
    public WorkspaceResponse updateWorkspace(
            @PathVariable UUID workspaceId,
            @Valid @RequestBody UpdateWorkspaceRequest request,
            Authentication authentication) {

        return workspaceService.updateWorkspace(
                workspaceId,
                request,
                authentication
        );
    }


    @DeleteMapping("/{workspaceId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteWorkspace(
            @PathVariable UUID workspaceId,
            Authentication authentication) {

        workspaceService.deleteWorkspace(
                workspaceId,
                authentication
        );
    }


}