package com.sprintflow.backend.service;

import com.sprintflow.backend.dto.project.*;
import com.sprintflow.backend.entity.*;
import com.sprintflow.backend.enums.ProjectRole;
import com.sprintflow.backend.enums.ProjectStatus;
import com.sprintflow.backend.enums.WorkspaceRole;
import com.sprintflow.backend.repository.ProjectMemberRepository;
import com.sprintflow.backend.repository.ProjectRepository;
import com.sprintflow.backend.repository.UserRepository;
import com.sprintflow.backend.repository.WorkspaceMemberRepository;
import com.sprintflow.backend.repository.WorkspaceRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final UserRepository userRepository;


    public ProjectService(
            ProjectRepository projectRepository,
            ProjectMemberRepository projectMemberRepository,
            WorkspaceRepository workspaceRepository,
            WorkspaceMemberRepository workspaceMemberRepository,
            UserRepository userRepository) {

        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.workspaceRepository = workspaceRepository;
        this.workspaceMemberRepository = workspaceMemberRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ProjectResponse createProject(
            UUID workspaceId,
            CreateProjectRequest request,
            Authentication authentication) {

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("Authenticated user not found"));

        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() ->
                        new RuntimeException("Workspace not found"));

        WorkspaceMember workspaceMember =
                workspaceMemberRepository
                        .findByUserAndWorkspace(user, workspace)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "You are not a member of this workspace"));

        if (workspaceMember.getRole() != WorkspaceRole.OWNER &&
                workspaceMember.getRole() != WorkspaceRole.ADMIN) {

            throw new RuntimeException(
                    "Only owner or admin can create projects");
        }

        Project project = new Project();

        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setStatus(ProjectStatus.ACTIVE);
        project.setWorkspace(workspace);

        Project savedProject = projectRepository.save(project);

        ProjectMember projectMember = new ProjectMember();

        projectMember.setProject(savedProject);
        projectMember.setUser(user);
        projectMember.setProjectRole(ProjectRole.MANAGER);

        projectMemberRepository.save(projectMember);

        ProjectResponse response = new ProjectResponse();

        response.setId(savedProject.getId());
        response.setName(savedProject.getName());
        response.setDescription(savedProject.getDescription());
        response.setStatus(savedProject.getStatus());
        response.setCreatedAt(savedProject.getCreatedAt());
        response.setUpdatedAt(savedProject.getUpdatedAt());
        response.setWorkspaceId(workspace.getId());

        return response;
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> getProjects(
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
                        new RuntimeException(
                                "You are not a member of this workspace"));

        List<Project> projects =
                projectRepository.findByWorkspace(workspace);

        return projects.stream()
                .map(project -> {

                    ProjectResponse response = new ProjectResponse();

                    response.setId(project.getId());
                    response.setName(project.getName());
                    response.setDescription(project.getDescription());
                    response.setStatus(project.getStatus());
                    response.setCreatedAt(project.getCreatedAt());
                    response.setUpdatedAt(project.getUpdatedAt());
                    response.setWorkspaceId(workspace.getId());

                    return response;
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public ProjectResponse getProject(
            UUID projectId,
            Authentication authentication) {

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("Authenticated user not found"));

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() ->
                        new RuntimeException("Project not found"));

        workspaceMemberRepository
                .findByUserAndWorkspace(user, project.getWorkspace())
                .orElseThrow(() ->
                        new RuntimeException(
                                "You are not a member of this workspace"));

        ProjectResponse response = new ProjectResponse();

        response.setId(project.getId());
        response.setName(project.getName());
        response.setDescription(project.getDescription());
        response.setStatus(project.getStatus());
        response.setCreatedAt(project.getCreatedAt());
        response.setUpdatedAt(project.getUpdatedAt());
        response.setWorkspaceId(project.getWorkspace().getId());

        return response;
    }

    @Transactional
    public ProjectResponse updateProject(
            UUID projectId,
            UpdateProjectRequest request,
            Authentication authentication) {

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("Authenticated user not found"));

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() ->
                        new RuntimeException("Project not found"));

        Workspace workspace = project.getWorkspace();

        WorkspaceMember member =
                workspaceMemberRepository
                        .findByUserAndWorkspace(user, workspace)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "You are not a member of this workspace"));

        if (member.getRole() != WorkspaceRole.OWNER &&
                member.getRole() != WorkspaceRole.ADMIN) {

            throw new RuntimeException(
                    "Only owner or admin can update the project");
        }

        project.setName(request.getName());
        project.setDescription(request.getDescription());

        if (request.getStatus() != null) {
            project.setStatus(request.getStatus());
        }

        Project savedProject = projectRepository.save(project);

        ProjectResponse response = new ProjectResponse();

        response.setId(savedProject.getId());
        response.setName(savedProject.getName());
        response.setDescription(savedProject.getDescription());
        response.setStatus(savedProject.getStatus());
        response.setCreatedAt(savedProject.getCreatedAt());
        response.setUpdatedAt(savedProject.getUpdatedAt());
        response.setWorkspaceId(workspace.getId());

        return response;
    }

    @Transactional
    public void deleteProject(
            UUID projectId,
            Authentication authentication) {

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("Authenticated user not found"));

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() ->
                        new RuntimeException("Project not found"));

        Workspace workspace = project.getWorkspace();

        WorkspaceMember member =
                workspaceMemberRepository
                        .findByUserAndWorkspace(user, workspace)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "You are not a member of this workspace"));

        if (member.getRole() != WorkspaceRole.OWNER &&
                member.getRole() != WorkspaceRole.ADMIN) {

            throw new RuntimeException(
                    "Only owner or admin can delete the project");
        }

        projectMemberRepository.deleteByProject(project);

        projectRepository.delete(project);
    }

    @Transactional
    public void addProjectMember(
            UUID projectId,
            AddProjectMemberRequest request,
            Authentication authentication) {

        String email = authentication.getName();

        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("Authenticated user not found"));

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() ->
                        new RuntimeException("Project not found"));

        Workspace workspace = project.getWorkspace();


        WorkspaceMember currentMember =
                workspaceMemberRepository
                        .findByUserAndWorkspace(currentUser, workspace)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "You are not a member of this workspace"));


        if (currentMember.getRole() != WorkspaceRole.OWNER &&
                currentMember.getRole() != WorkspaceRole.ADMIN) {

            throw new RuntimeException(
                    "Only owner or admin can add project members");
        }


        User userToAdd = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new RuntimeException("User not found"));


        workspaceMemberRepository
                .findByUserAndWorkspace(userToAdd, workspace)
                .orElseThrow(() ->
                        new RuntimeException(
                                "User is not a member of this workspace"));


        if (projectMemberRepository
                .findByUserAndProject(userToAdd, project)
                .isPresent()) {

            throw new RuntimeException(
                    "User is already a member of this project");
        }


        ProjectMember projectMember = new ProjectMember();

        projectMember.setUser(userToAdd);
        projectMember.setProject(project);
        projectMember.setProjectRole(request.getProjectRole());

        projectMemberRepository.save(projectMember);
    }

    public List<ProjectMemberResponse> getProjectMembers(
            UUID projectId,
            Authentication authentication) {

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() ->
                        new RuntimeException("Project not found"));

        User currentUser = userRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        // Make sure current user belongs to this project
        projectMemberRepository
                .findByUserAndProject(currentUser, project)
                .orElseThrow(() ->
                        new RuntimeException(
                                "You are not a member of this project"
                        ));

        return projectMemberRepository
                .findByProject(project)
                .stream()
                .map(member -> {

                    ProjectMemberResponse response =
                            new ProjectMemberResponse();

                    response.setUserId(member.getUser().getId());
                    response.setName(member.getUser().getName());
                    response.setEmail(member.getUser().getEmail());
                    response.setProjectRole(member.getProjectRole());

                    return response;
                })
                .toList();
    }

    public ProjectMemberResponse updateProjectMemberRole(
            UUID projectId,
            UUID userId,
            UpdateProjectMemberRequest request,
            Authentication authentication) {

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() ->
                        new RuntimeException("Project not found"));

        User currentUser = userRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        ProjectMember currentMember =
                projectMemberRepository
                        .findByUserAndProject(currentUser, project)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "You are not a member of this project"
                                ));

        if (currentMember.getProjectRole() != ProjectRole.MANAGER) {
            throw new RuntimeException(
                    "Only project managers can change roles"
            );
        }

        User targetUser = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        ProjectMember targetMember =
                projectMemberRepository
                        .findByUserAndProject(targetUser, project)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "User is not a member of this project"
                                ));

        targetMember.setProjectRole(request.getProjectRole());

        ProjectMember savedMember =
                projectMemberRepository.save(targetMember);

        ProjectMemberResponse response =
                new ProjectMemberResponse();

        response.setUserId(savedMember.getUser().getId());
        response.setName(savedMember.getUser().getName());
        response.setEmail(savedMember.getUser().getEmail());
        response.setProjectRole(savedMember.getProjectRole());

        return response;
    }

    @Transactional
    public void removeProjectMember(
            UUID projectId,
            UUID userId,
            Authentication authentication) {

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() ->
                        new RuntimeException("Project not found"));

        User currentUser = userRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        ProjectMember currentMember =
                projectMemberRepository
                        .findByUserAndProject(currentUser, project)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "You are not a member of this project"
                                ));

        if (currentMember.getProjectRole() != ProjectRole.MANAGER) {
            throw new RuntimeException(
                    "Only project managers can remove members"
            );
        }

        User targetUser = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        ProjectMember targetMember =
                projectMemberRepository
                        .findByUserAndProject(targetUser, project)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "User is not a member of this project"
                                ));

        if (targetMember.getProjectRole() == ProjectRole.MANAGER) {
            throw new RuntimeException(
                    "Cannot remove a project manager"
            );
        }

        projectMemberRepository.delete(targetMember);
    }
}
