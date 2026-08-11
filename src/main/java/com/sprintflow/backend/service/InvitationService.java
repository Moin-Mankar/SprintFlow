package com.sprintflow.backend.service;

import com.sprintflow.backend.entity.Invitation;
import com.sprintflow.backend.entity.User;
import com.sprintflow.backend.entity.Workspace;
import com.sprintflow.backend.entity.WorkspaceMember;
import com.sprintflow.backend.enums.WorkspaceRole;
import com.sprintflow.backend.repository.InvitationRepository;
import com.sprintflow.backend.repository.UserRepository;
import com.sprintflow.backend.repository.WorkspaceMemberRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class InvitationService {

    private final InvitationRepository invitationRepository;
    private final UserRepository userRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;

    public InvitationService(
            InvitationRepository invitationRepository,
            UserRepository userRepository,
            WorkspaceMemberRepository workspaceMemberRepository) {

        this.invitationRepository = invitationRepository;
        this.userRepository = userRepository;
        this.workspaceMemberRepository = workspaceMemberRepository;
    }

    @Transactional
    public void joinWorkspace(
            String token,
            Authentication authentication) {

        Invitation invitation = invitationRepository.findByToken(token)
                .orElseThrow(() ->
                        new RuntimeException("Invalid invitation"));

        if (invitation.isUsed()) {
            throw new RuntimeException("Invitation has already been used");
        }

        if (invitation.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Invitation has expired");
        }

        String email = authentication.getName();

        if (!email.equalsIgnoreCase(invitation.getEmail())) {
            throw new RuntimeException(
                    "This invitation was not created for your email");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("Authenticated user not found"));

        if (workspaceMemberRepository
                .findByUserAndWorkspace(user, invitation.getWorkspace())
                .isPresent()) {

            throw new RuntimeException(
                    "User is already a member of this workspace");
        }

        WorkspaceMember member = new WorkspaceMember();

        member.setUser(user);
        member.setWorkspace(invitation.getWorkspace());
        member.setRole(WorkspaceRole.MEMBER);

        workspaceMemberRepository.save(member);

        invitation.setUsed(true);
        invitationRepository.save(invitation);
    }

    @Transactional
    public void acceptInvitation(
            String token,
            Authentication authentication) {

        // Get the currently logged-in user
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("Authenticated user not found"));

        // Find invitation
        Invitation invitation = invitationRepository.findByToken(token)
                .orElseThrow(() ->
                        new RuntimeException("Invitation not found"));

        // Check whether already used
        if (invitation.isUsed()) {
            throw new RuntimeException("Invitation has already been used");
        }

        // Check expiration
        if (invitation.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Invitation has expired");
        }

        // Make sure the invitation belongs to this user
        if (!invitation.getEmail().equalsIgnoreCase(user.getEmail())) {
            throw new RuntimeException(
                    "This invitation is not for this user");
        }

        Workspace workspace = invitation.getWorkspace();

        // Prevent duplicate workspace membership
        if (workspaceMemberRepository
                .findByUserAndWorkspace(user, workspace)
                .isPresent()) {

            throw new RuntimeException(
                    "User is already a member of this workspace");
        }

        // Create workspace membership
        WorkspaceMember member = new WorkspaceMember();

        member.setUser(user);
        member.setWorkspace(workspace);
        member.setRole(WorkspaceRole.MEMBER);

        workspaceMemberRepository.save(member);

        // Mark invitation as used
        invitation.setUsed(true);

        invitationRepository.save(invitation);
    }
}