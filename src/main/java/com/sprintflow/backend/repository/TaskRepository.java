package com.sprintflow.backend.repository;

import com.sprintflow.backend.entity.Board;
import com.sprintflow.backend.entity.Task;
import com.sprintflow.backend.entity.Workspace;
import com.sprintflow.backend.enums.TaskPriority;
import com.sprintflow.backend.enums.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface TaskRepository extends JpaRepository<Task, UUID> {

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
}