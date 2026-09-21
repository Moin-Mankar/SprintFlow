package com.sprintflow.backend.repository;

import com.sprintflow.backend.entity.Board;
import com.sprintflow.backend.entity.Project;
import com.sprintflow.backend.entity.Task;
import com.sprintflow.backend.entity.Workspace;
import com.sprintflow.backend.enums.TaskPriority;
import com.sprintflow.backend.enums.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface TaskRepository extends JpaRepository<Task, UUID> ,JpaSpecificationExecutor<Task>{

    List<Task> findByBoard(Board board);

    long countByBoard_Project_Workspace(Workspace workspace);

    long countByBoard_Project_WorkspaceAndTaskStatus(
            Workspace workspace,
            TaskStatus taskStatus
    );

    long countByBoard_Project_WorkspaceAndDueDateBeforeAndTaskStatusNot(
            Workspace workspace,
            LocalDateTime dateTime,
            TaskStatus taskStatus
    );

    List<Task> findByBoardAndTaskStatus(
            Board board,
            TaskStatus taskStatus
    );

    List<Task> findByBoardAndTaskPriority(
            Board board,
            TaskPriority taskPriority
    );

    List<Task> findByBoardAndTitleContainingIgnoreCase(
            Board board,
            String title
    );

    long countByBoard_Project(Project project);

    long countByBoard_ProjectAndTaskStatus(
            Project project,
            TaskStatus taskStatus
    );

    long countByBoard_ProjectAndDueDateBeforeAndTaskStatusNot(
            Project project,
            LocalDateTime dateTime,
            TaskStatus taskStatus
    );

    long countByBoard_ProjectAndTaskPriority(
            Project project,
            TaskPriority taskPriority
    );

    @Query("""
        SELECT t.assignee.id, t.assignee.name, COUNT(t.id)
        FROM Task t
        WHERE t.board.project.id = :projectId
        AND t.assignee IS NOT NULL
        GROUP BY t.assignee.id, t.assignee.name
        ORDER BY COUNT(t.id) DESC
        """)
    List<Object[]> findUserWorkloadByProject(UUID projectId);
}