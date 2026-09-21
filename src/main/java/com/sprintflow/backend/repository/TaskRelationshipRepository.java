package com.sprintflow.backend.repository;

import com.sprintflow.backend.entity.TaskRelationship;
import com.sprintflow.backend.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface TaskRelationshipRepository extends JpaRepository<TaskRelationship, UUID> {
    List<TaskRelationship> findBySourceTask(Task task);

    List<TaskRelationship> findByTargetTask(Task task);

    boolean existsBySourceTaskAndTargetTask(Task sourceTask, Task targetTask);


    @Query("""
        SELECT COUNT(DISTINCT r.targetTask.id)
        FROM TaskRelationship r
        WHERE r.targetTask.board.project.id = :projectId
        AND r.sourceTask.taskStatus <> com.sprintflow.backend.enums.TaskStatus.DONE
        """)
    long countBlockedTasksByProject(UUID projectId);
}