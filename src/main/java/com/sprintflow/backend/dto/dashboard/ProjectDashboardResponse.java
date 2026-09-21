package com.sprintflow.backend.dto.dashboard;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
public class ProjectDashboardResponse {

    private UUID projectId;
    private String projectName;

    private long totalTasks;
    private long todoTasks;
    private long inProgressTasks;
    private long inReviewTasks;
    private long completedTasks;
    private long overdueTasks;
    private long blockedTasks;

    private Map<String, Long> priorityDistribution;

    private List<UserWorkloadResponse> userWorkload;
}
