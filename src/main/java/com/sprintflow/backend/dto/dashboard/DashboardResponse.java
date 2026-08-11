package com.sprintflow.backend.dto.dashboard;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DashboardResponse {

    private long totalProjects;

    private long totalTasks;

    private long todoTasks;

    private long inProgressTasks;

    private long inReviewTasks;

    private long completedTasks;

    private long overdueTasks;
}
