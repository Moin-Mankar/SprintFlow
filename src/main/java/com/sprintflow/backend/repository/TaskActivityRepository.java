package com.sprintflow.backend.repository;

import com.sprintflow.backend.entity.Task;
import com.sprintflow.backend.entity.TaskActivity;
import com.sprintflow.backend.entity.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TaskActivityRepository extends JpaRepository<TaskActivity, UUID> {

    List<TaskActivity> findByTaskOrderByCreatedAtAsc(Task task);

    void deleteByTask(Task task);

    void deleteByTask_Board_Project_Workspace(Workspace workspace);
}