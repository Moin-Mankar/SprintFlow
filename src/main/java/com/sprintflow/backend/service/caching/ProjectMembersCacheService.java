package com.sprintflow.backend.service.caching;

import com.sprintflow.backend.dto.project.ProjectMemberResponse;
import com.sprintflow.backend.entity.Project;
import com.sprintflow.backend.repository.ProjectMemberRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ProjectMembersCacheService {

    private final ProjectMemberRepository projectMemberRepository;

    public ProjectMembersCacheService(
            ProjectMemberRepository projectMemberRepository) {

        this.projectMemberRepository = projectMemberRepository;
    }

    @Cacheable(
            value = "projectMembers",
            key = "#projectId"
    )
    public List<ProjectMemberResponse> getProjectMembers(
            UUID projectId,
            Project project) {

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
                .collect(Collectors.toList());
    }

    @CacheEvict(
            value = "projectMembers",
            key = "#projectId"
    )
    public void evictProjectMembers(UUID projectId) {
    }
}
