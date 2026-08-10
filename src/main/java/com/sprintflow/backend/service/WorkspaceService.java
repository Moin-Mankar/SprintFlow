package com.sprintflow.backend.service;

import com.sprintflow.backend.dto.workspace.*;
import com.sprintflow.backend.entity.User;
import com.sprintflow.backend.entity.Workspace;
import com.sprintflow.backend.entity.WorkspaceMember;
import com.sprintflow.backend.enums.WorkspaceRole;
import com.sprintflow.backend.repository.InvitationRepository;
import com.sprintflow.backend.repository.UserRepository;
import com.sprintflow.backend.repository.WorkspaceMemberRepository;
import com.sprintflow.backend.repository.WorkspaceRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;
import com.sprintflow.backend.entity.Invitation;
import com.sprintflow.backend.repository.InvitationRepository;

import java.time.LocalDateTime;


@Service
public class WorkspaceService {

    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final UserRepository userRepository;
    private final InvitationRepository invitationRepository;

    public WorkspaceService(
            WorkspaceRepository workspaceRepository,
            WorkspaceMemberRepository workspaceMemberRepository,
            UserRepository userRepository, InvitationRepository invitationRepository) {

        this.workspaceRepository = workspaceRepository;
        this.workspaceMemberRepository = workspaceMemberRepository;
        this.userRepository = userRepository;
        this.invitationRepository = invitationRepository;
    }

    @Transactional
    public Workspace createWorkspace(
            CreateWorkspaceRequest request,
            Authentication authentication) {

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("Authenticated user not found"));

        Workspace workspace = new Workspace();

        workspace.setName(request.getName());
        workspace.setDescription(request.getDescription());
        workspace.setWorkspaceType(request.getWorkspaceType());

        Workspace savedWorkspace = workspaceRepository.save(workspace);

        WorkspaceMember member = new WorkspaceMember();

        member.setUser(user);
        member.setWorkspace(savedWorkspace);
        member.setRole(WorkspaceRole.OWNER);

        workspaceMemberRepository.save(member);

        return savedWorkspace;
    }

    public List<WorkspaceResponse> getMyWorkspaces(Authentication authentication) {

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("Authenticated user not found"));

        return workspaceMemberRepository.findByUser(user)
                .stream()
                .map(WorkspaceMember::getWorkspace)
                .map(workspace -> {

                    WorkspaceResponse response = new WorkspaceResponse();

                    response.setId(workspace.getId());
                    response.setName(workspace.getName());
                    response.setDescription(workspace.getDescription());
                    response.setWorkspaceType(workspace.getWorkspaceType());
                    response.setCreatedAt(workspace.getCreatedAt());
                    response.setUpdatedAt(workspace.getUpdatedAt());

                    return response;
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public WorkspaceResponse getWorkspace(
            UUID workspaceId,
            Authentication authentication) {

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("Authenticated user not found"));

        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() ->
                        new RuntimeException("Workspace not found"));

        workspaceMemberRepository
                .findByUserAndWorkspace(user, workspace)
                .orElseThrow(() ->
                        new RuntimeException("You are not a member of this workspace"));

        WorkspaceResponse response = new WorkspaceResponse();

        response.setId(workspace.getId());
        response.setName(workspace.getName());
        response.setDescription(workspace.getDescription());
        response.setWorkspaceType(workspace.getWorkspaceType());
        response.setCreatedAt(workspace.getCreatedAt());
        response.setUpdatedAt(workspace.getUpdatedAt());

        return response;
    }

    @Transactional
    public WorkspaceResponse updateWorkspace(
            UUID workspaceId,
            UpdateWorkspaceRequest request,
            Authentication authentication) {

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("Authenticated user not found"));

        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() ->
                        new RuntimeException("Workspace not found"));

        WorkspaceMember member = workspaceMemberRepository
                .findByUserAndWorkspace(user, workspace)
                .orElseThrow(() ->
                        new RuntimeException("You are not a member of this workspace"));

        if (member.getRole() != WorkspaceRole.OWNER &&
                member.getRole() != WorkspaceRole.ADMIN) {
            throw new RuntimeException("You do not have permission to update this workspace");
        }

        workspace.setName(request.getName());
        workspace.setDescription(request.getDescription());

        Workspace savedWorkspace = workspaceRepository.save(workspace);

        WorkspaceResponse response = new WorkspaceResponse();

        response.setId(savedWorkspace.getId());
        response.setName(savedWorkspace.getName());
        response.setDescription(savedWorkspace.getDescription());
        response.setWorkspaceType(savedWorkspace.getWorkspaceType());
        response.setCreatedAt(savedWorkspace.getCreatedAt());
        response.setUpdatedAt(savedWorkspace.getUpdatedAt());

        return response;
    }

    @Transactional
    public void deleteWorkspace(
            UUID workspaceId,
            Authentication authentication) {

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("Authenticated user not found"));

        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() ->
                        new RuntimeException("Workspace not found"));

        WorkspaceMember member = workspaceMemberRepository
                .findByUserAndWorkspace(user, workspace)
                .orElseThrow(() ->
                        new RuntimeException("You are not a member of this workspace"));

        if (member.getRole() != WorkspaceRole.OWNER) {
            throw new RuntimeException(
                    "Only the workspace owner can delete the workspace");
        }

        workspaceMemberRepository.deleteByWorkspace(workspace);

        workspaceRepository.delete(workspace);
    }

    @Transactional
    public InvitationResponse createInvitation(
            UUID workspaceId,
            CreateInvitationRequest request,
            Authentication authentication) {

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("Authenticated user not found"));

        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() ->
                        new RuntimeException("Workspace not found"));

        WorkspaceMember member = workspaceMemberRepository
                .findByUserAndWorkspace(user, workspace)
                .orElseThrow(() ->
                        new RuntimeException("You are not a member of this workspace"));

        if (member.getRole() != WorkspaceRole.OWNER &&
                member.getRole() != WorkspaceRole.ADMIN) {

            throw new RuntimeException(
                    "Only owner or admin can invite users");
        }

        Invitation invitation = new Invitation();

        invitation.setToken(UUID.randomUUID().toString());
        invitation.setEmail(request.getEmail());
        invitation.setWorkspace(workspace);
        invitation.setCreatedBy(user);
        invitation.setExpiresAt(
                LocalDateTime.now().plusDays(7)
        );

        Invitation savedInvitation = invitationRepository.save(invitation);

        InvitationResponse response = new InvitationResponse();

        response.setId(savedInvitation.getId());
        response.setToken(savedInvitation.getToken());
        response.setEmail(savedInvitation.getEmail());
        response.setUsed(savedInvitation.isUsed());
        response.setExpiresAt(savedInvitation.getExpiresAt());
        response.setCreatedAt(savedInvitation.getCreatedAt());
        response.setWorkspaceId(savedInvitation.getWorkspace().getId());
        response.setCreatedBy(savedInvitation.getCreatedBy().getId());

        return response;
    }
}