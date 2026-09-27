package com.sprintflow.backend.service;

import com.sprintflow.backend.entity.User;
import com.sprintflow.backend.entity.Workspace;
import com.sprintflow.backend.entity.WorkspaceMember;
import com.sprintflow.backend.enums.WorkspaceRole;
import com.sprintflow.backend.exception.ForbiddenException;
import com.sprintflow.backend.exception.ResourceNotFoundException;
import com.sprintflow.backend.repository.BoardRepository;
import com.sprintflow.backend.repository.CommentRepository;
import com.sprintflow.backend.repository.InvitationRepository;
import com.sprintflow.backend.repository.ProjectMemberRepository;
import com.sprintflow.backend.repository.ProjectRepository;
import com.sprintflow.backend.repository.TaskActivityRepository;
import com.sprintflow.backend.repository.TaskRelationshipRepository;
import com.sprintflow.backend.repository.TaskRepository;
import com.sprintflow.backend.repository.UserRepository;
import com.sprintflow.backend.repository.WorkspaceMemberRepository;
import com.sprintflow.backend.repository.WorkspaceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Covers the OWNER-only authorization and the child-first teardown of
 * WorkspaceService.deleteWorkspace. Every repository is mocked, so the delete
 * calls themselves are asserted as scoped instructions; the resulting database
 * state is covered by the live API verification.
 */
class WorkspaceDeleteServiceTest {

    private static final String EMAIL = "owner@sprintflow.test";
    private static final UUID WORKSPACE_ID = UUID.randomUUID();

    private WorkspaceRepository workspaceRepository;
    private WorkspaceMemberRepository workspaceMemberRepository;
    private UserRepository userRepository;
    private InvitationRepository invitationRepository;
    private ProjectRepository projectRepository;
    private ProjectMemberRepository projectMemberRepository;
    private BoardRepository boardRepository;
    private TaskRepository taskRepository;
    private TaskRelationshipRepository taskRelationshipRepository;
    private TaskActivityRepository taskActivityRepository;
    private CommentRepository commentRepository;

    private WorkspaceService service;
    private Workspace workspace;
    private User owner;

    @BeforeEach
    void setUp() {

        workspaceRepository = mock(WorkspaceRepository.class);
        workspaceMemberRepository = mock(WorkspaceMemberRepository.class);
        userRepository = mock(UserRepository.class);
        invitationRepository = mock(InvitationRepository.class);
        projectRepository = mock(ProjectRepository.class);
        projectMemberRepository = mock(ProjectMemberRepository.class);
        boardRepository = mock(BoardRepository.class);
        taskRepository = mock(TaskRepository.class);
        taskRelationshipRepository = mock(TaskRelationshipRepository.class);
        taskActivityRepository = mock(TaskActivityRepository.class);
        commentRepository = mock(CommentRepository.class);

        service = new WorkspaceService(
                workspaceRepository,
                workspaceMemberRepository,
                userRepository,
                invitationRepository,
                projectRepository,
                projectMemberRepository,
                boardRepository,
                taskRepository,
                taskRelationshipRepository,
                taskActivityRepository,
                commentRepository
        );

        owner = new User();
        owner.setId(UUID.randomUUID());
        owner.setEmail(EMAIL);

        workspace = new Workspace();
        workspace.setId(WORKSPACE_ID);
        workspace.setName("Design QA Sep24");

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(owner));
        when(workspaceRepository.findById(WORKSPACE_ID)).thenReturn(Optional.of(workspace));
    }

    private Authentication authentication() {
        return new UsernamePasswordAuthenticationToken(EMAIL, null);
    }

    private void memberWithRole(WorkspaceRole role) {
        WorkspaceMember member = new WorkspaceMember();
        member.setUser(owner);
        member.setWorkspace(workspace);
        member.setRole(role);
        when(workspaceMemberRepository.findByUserAndWorkspace(owner, workspace))
                .thenReturn(Optional.of(member));
    }

    @Test
    void ownerDeletesWorkspaceTearingDownHierarchyChildFirst() {

        memberWithRole(WorkspaceRole.OWNER);

        service.deleteWorkspace(WORKSPACE_ID, authentication());

        InOrder order = inOrder(
                taskRelationshipRepository,
                commentRepository,
                taskActivityRepository,
                taskRepository,
                boardRepository,
                projectMemberRepository,
                invitationRepository,
                projectRepository,
                workspaceMemberRepository,
                workspaceRepository
        );

        order.verify(taskRelationshipRepository)
                .deleteBySourceTask_Board_Project_Workspace(workspace);
        order.verify(taskRelationshipRepository)
                .deleteByTargetTask_Board_Project_Workspace(workspace);
        order.verify(commentRepository)
                .deleteByTask_Board_Project_Workspace(workspace);
        order.verify(taskActivityRepository)
                .deleteByTask_Board_Project_Workspace(workspace);
        order.verify(taskRepository)
                .deleteByBoard_Project_Workspace(workspace);
        order.verify(boardRepository)
                .deleteByProject_Workspace(workspace);
        order.verify(projectMemberRepository)
                .deleteByProject_Workspace(workspace);
        order.verify(invitationRepository).deleteByWorkspace(workspace);
        order.verify(projectRepository).deleteByWorkspace(workspace);
        order.verify(workspaceMemberRepository).deleteByWorkspace(workspace);
        order.verify(workspaceRepository).delete(workspace);
        order.verifyNoMoreInteractions();
    }

    @Test
    void everyDeleteIsScopedToTheWorkspaceBeingDeleted() {

        memberWithRole(WorkspaceRole.OWNER);

        service.deleteWorkspace(WORKSPACE_ID, authentication());

        ArgumentCaptor<Workspace> tasks = ArgumentCaptor.forClass(Workspace.class);
        ArgumentCaptor<Workspace> boards = ArgumentCaptor.forClass(Workspace.class);
        ArgumentCaptor<Workspace> projects = ArgumentCaptor.forClass(Workspace.class);
        ArgumentCaptor<Workspace> invitations = ArgumentCaptor.forClass(Workspace.class);

        verify(taskRepository).deleteByBoard_Project_Workspace(tasks.capture());
        verify(boardRepository).deleteByProject_Workspace(boards.capture());
        verify(projectRepository).deleteByWorkspace(projects.capture());
        verify(invitationRepository).deleteByWorkspace(invitations.capture());

        assertThat(tasks.getAllValues()).containsExactly(workspace);
        assertThat(boards.getAllValues()).containsExactly(workspace);
        assertThat(projects.getAllValues()).containsExactly(workspace);
        assertThat(invitations.getAllValues()).containsExactly(workspace);
    }

    @Test
    void adminRoleCannotDeleteWorkspace() {

        memberWithRole(WorkspaceRole.ADMIN);

        assertThatThrownBy(() -> service.deleteWorkspace(WORKSPACE_ID, authentication()))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("Only the workspace owner can delete the workspace");

        verify(workspaceRepository, never()).delete(any());
        verifyNoInteractions(projectRepository, boardRepository, taskRepository);
    }

    @Test
    void memberRoleCannotDeleteWorkspace() {

        memberWithRole(WorkspaceRole.MEMBER);

        assertThatThrownBy(() -> service.deleteWorkspace(WORKSPACE_ID, authentication()))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("Only the workspace owner can delete the workspace");

        verify(workspaceRepository, never()).delete(any());
    }

    @Test
    void nonMemberCannotDeleteWorkspace() {

        when(workspaceMemberRepository.findByUserAndWorkspace(owner, workspace))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteWorkspace(WORKSPACE_ID, authentication()))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("You are not a member of this workspace");

        verify(workspaceRepository, never()).delete(any());
        verifyNoInteractions(commentRepository, taskActivityRepository);
    }

    @Test
    void unknownWorkspaceIsNotFound() {

        when(workspaceRepository.findById(WORKSPACE_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteWorkspace(WORKSPACE_ID, authentication()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Workspace not found");

        verifyNoInteractions(workspaceMemberRepository, projectRepository);
    }

    @Test
    void missingAuthenticatedPrincipalIsNotFound() {

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteWorkspace(WORKSPACE_ID, authentication()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Authenticated user not found");

        verifyNoInteractions(workspaceRepository, projectRepository);
    }

    @Test
    void constraintViolationPropagatesBeforeTheWorkspaceIsDeleted() {

        memberWithRole(WorkspaceRole.OWNER);
        org.mockito.Mockito.doThrow(new DataIntegrityViolationException("fk task -> board"))
                .when(taskRepository).deleteByBoard_Project_Workspace(workspace);

        assertThatThrownBy(() -> service.deleteWorkspace(WORKSPACE_ID, authentication()))
                .isInstanceOf(DataIntegrityViolationException.class);

        verify(workspaceRepository, never()).delete(any());
        verify(workspaceMemberRepository, never()).deleteByWorkspace(any());
        verify(boardRepository, never()).deleteByProject_Workspace(any());
        verify(projectRepository, never()).deleteByWorkspace(any());
    }
}
