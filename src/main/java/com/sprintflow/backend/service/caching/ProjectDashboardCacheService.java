package com.sprintflow.backend.service.caching;


import com.sprintflow.backend.dto.dashboard.ProjectDashboardResponse;
import com.sprintflow.backend.dto.dashboard.UserWorkloadResponse;
import com.sprintflow.backend.entity.Project;
import com.sprintflow.backend.enums.TaskPriority;
import com.sprintflow.backend.enums.TaskStatus;
import com.sprintflow.backend.repository.TaskRelationshipRepository;
import com.sprintflow.backend.repository.TaskRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ProjectDashboardCacheService {

    private final TaskRepository taskRepository;
    private final TaskRelationshipRepository taskRelationshipRepository;

    public ProjectDashboardCacheService(
            TaskRepository taskRepository,
            TaskRelationshipRepository taskRelationshipRepository) {

        this.taskRepository = taskRepository;
        this.taskRelationshipRepository = taskRelationshipRepository;
    }

    @Cacheable(
            value = "projectDashboard",
            key = "#projectId"
    )
    public ProjectDashboardResponse getDashboard(
            UUID projectId,
            Project project) {

        long totalTasks =
                taskRepository.countByBoard_Project(project);

        long todoTasks =
                taskRepository.countByBoard_ProjectAndTaskStatus(
                        project,
                        TaskStatus.TODO
                );

        long inProgressTasks =
                taskRepository.countByBoard_ProjectAndTaskStatus(
                        project,
                        TaskStatus.IN_PROGRESS
                );

        long inReviewTasks =
                taskRepository.countByBoard_ProjectAndTaskStatus(
                        project,
                        TaskStatus.IN_REVIEW
                );

        long completedTasks =
                taskRepository.countByBoard_ProjectAndTaskStatus(
                        project,
                        TaskStatus.DONE
                );

        long overdueTasks =
                taskRepository
                        .countByBoard_ProjectAndDueDateBeforeAndTaskStatusNot(
                                project,
                                LocalDateTime.now(),
                                TaskStatus.DONE
                        );

        long blockedTasks =
                taskRelationshipRepository
                        .countBlockedTasksByProject(projectId);

        Map<String, Long> priorityDistribution =
                new LinkedHashMap<>();

        priorityDistribution.put(
                TaskPriority.HIGH.name(),
                taskRepository.countByBoard_ProjectAndTaskPriority(
                        project,
                        TaskPriority.HIGH
                )
        );

        priorityDistribution.put(
                TaskPriority.MEDIUM.name(),
                taskRepository.countByBoard_ProjectAndTaskPriority(
                        project,
                        TaskPriority.MEDIUM
                )
        );

        priorityDistribution.put(
                TaskPriority.LOW.name(),
                taskRepository.countByBoard_ProjectAndTaskPriority(
                        project,
                        TaskPriority.LOW
                )
        );

        List<UserWorkloadResponse> userWorkload =
                taskRepository.findUserWorkloadByProject(projectId)
                        .stream()
                        .map(row -> {

                            UserWorkloadResponse workload =
                                    new UserWorkloadResponse();

                            workload.setUserId((UUID) row[0]);
                            workload.setUserName((String) row[1]);
                            workload.setAssignedTasks((Long) row[2]);

                            return workload;
                        })
                        .toList();

        ProjectDashboardResponse response =
                new ProjectDashboardResponse();

        response.setProjectId(project.getId());
        response.setProjectName(project.getName());

        response.setTotalTasks(totalTasks);
        response.setTodoTasks(todoTasks);
        response.setInProgressTasks(inProgressTasks);
        response.setInReviewTasks(inReviewTasks);
        response.setCompletedTasks(completedTasks);
        response.setOverdueTasks(overdueTasks);
        response.setBlockedTasks(blockedTasks);

        response.setPriorityDistribution(priorityDistribution);
        response.setUserWorkload(userWorkload);

        return response;
    }

    @CacheEvict(
            value = "projectDashboard",
            key = "#projectId"
    )
    public void evictDashboard(UUID projectId) {
    }
}
