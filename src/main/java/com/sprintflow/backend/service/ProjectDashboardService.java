package com.sprintflow.backend.service;

import com.sprintflow.backend.dto.dashboard.ProjectDashboardResponse;
import com.sprintflow.backend.entity.Project;
import com.sprintflow.backend.entity.User;
import com.sprintflow.backend.repository.ProjectRepository;
import com.sprintflow.backend.repository.ProjectMemberRepository;
import com.sprintflow.backend.repository.UserRepository;
import com.sprintflow.backend.service.caching.ProjectDashboardCacheService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;


import java.util.UUID;

@Service
public class ProjectDashboardService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;
    private final ProjectDashboardCacheService projectDashboardCacheService;

    public ProjectDashboardService(
            ProjectRepository projectRepository,
            ProjectMemberRepository projectMemberRepository,
            UserRepository userRepository,
            ProjectDashboardCacheService projectDashboardCacheService) {

        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.userRepository = userRepository;
        this.projectDashboardCacheService = projectDashboardCacheService;
    }

    public ProjectDashboardResponse getDashboard(
            UUID projectId,
            Authentication authentication) {

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() ->
                        new RuntimeException("Project not found"));

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        projectMemberRepository
                .findByUserAndProject(user, project)
                .orElseThrow(() ->
                        new RuntimeException(
                                "You are not a member of this project"));

        return projectDashboardCacheService.getDashboard(
                projectId,
                project
        );
    }
}
