package com.sprintflow.backend.service;

import com.sprintflow.backend.dto.task.TaskActivityResponse;
import com.sprintflow.backend.entity.Task;
import com.sprintflow.backend.entity.TaskActivity;
import com.sprintflow.backend.entity.User;
import com.sprintflow.backend.enums.TaskActivityType;
import com.sprintflow.backend.repository.TaskActivityRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskActivityService {

    private final TaskActivityRepository taskActivityRepository;

    public TaskActivityService(TaskActivityRepository taskActivityRepository) {
        this.taskActivityRepository = taskActivityRepository;
    }

    public void recordActivity(User user, Task task, TaskActivityType taskActivityType,String description){
        TaskActivity activity = new TaskActivity();

        activity.setTask(task);
        activity.setUser(user);
        activity.setDescription(description);
        activity.setTaskActivityType(taskActivityType);

        taskActivityRepository.save(activity);
    }

    public List<TaskActivityResponse> getActivities(Task task){
        return taskActivityRepository.findByTaskOrderByCreatedAtAsc(task)
                .stream().map(this::toResponse).toList();
    }

    private TaskActivityResponse toResponse(TaskActivity activity){
        TaskActivityResponse response = new TaskActivityResponse();

        response.setId(activity.getId());
        response.setTaskActivityType(activity.getTaskActivityType());
        response.setDescription(activity.getDescription());
        response.setCreatedAt(activity.getCreatedAt());

        response.setUser_id(activity.getUser().getId());
        response.setUserName(activity.getUser().getName());

        return response;
    }
}
