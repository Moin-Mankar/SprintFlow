package com.sprintflow.backend.service;

import com.sprintflow.backend.dto.dashboard.DashboardResponse;
import com.sprintflow.backend.entity.User;
import com.sprintflow.backend.entity.Workspace;
import com.sprintflow.backend.enums.TaskStatus;
import com.sprintflow.backend.repository.ProjectRepository;
import com.sprintflow.backend.repository.TaskRepository;
import com.sprintflow.backend.repository.UserRepository;
import com.sprintflow.backend.repository.WorkspaceMemberRepository;
import com.sprintflow.backend.repository.WorkspaceRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class DashboardService {

    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final WorkspaceRepository workspaceRepository;
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public DashboardService(
            WorkspaceMemberRepository workspaceMemberRepository,
            WorkspaceRepository workspaceRepository,
            ProjectRepository projectRepository,
            TaskRepository taskRepository,
            UserRepository userRepository) {

        this.workspaceMemberRepository = workspaceMemberRepository;
        this.workspaceRepository = workspaceRepository;
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    public DashboardResponse getDashboard(
            UUID workspaceId,
            Authentication authentication) {

        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() ->
                        new RuntimeException("Workspace not found"));

        User user = userRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        workspaceMemberRepository
                .findByUserAndWorkspace(user, workspace)
                .orElseThrow(() ->
                        new RuntimeException(
                                "You are not a member of this workspace"));

        long totalProjects =
                projectRepository.countByWorkspace(workspace);

        long totalTasks =
                taskRepository.countByBoard_Project_Workspace(workspace);

        long todoTasks =
                taskRepository
                        .countByBoard_Project_WorkspaceAndTaskStatus(
                                workspace,
                                TaskStatus.TODO
                        );

        long inProgressTasks =
                taskRepository
                        .countByBoard_Project_WorkspaceAndTaskStatus(
                                workspace,
                                TaskStatus.IN_PROGRESS
                        );

        long inReviewTasks =
                taskRepository
                        .countByBoard_Project_WorkspaceAndTaskStatus(
                                workspace,
                                TaskStatus.IN_REVIEW
                        );

        long completedTasks =
                taskRepository
                        .countByBoard_Project_WorkspaceAndTaskStatus(
                                workspace,
                                TaskStatus.DONE
                        );

        long overdueTasks =
                taskRepository
                        .countByBoard_Project_WorkspaceAndDueDateBeforeAndTaskStatusNot(
                                workspace,
                                LocalDateTime.now(),
                                TaskStatus.DONE
                        );

        DashboardResponse response = new DashboardResponse();

        response.setTotalProjects(totalProjects);
        response.setTotalTasks(totalTasks);
        response.setTodoTasks(todoTasks);
        response.setInProgressTasks(inProgressTasks);
        response.setInReviewTasks(inReviewTasks);
        response.setCompletedTasks(completedTasks);
        response.setOverdueTasks(overdueTasks);

        return response;
    }
}