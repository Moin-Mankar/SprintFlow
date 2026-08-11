package com.sprintflow.backend.service;

import com.sprintflow.backend.dto.task.*;
import com.sprintflow.backend.entity.Board;
import com.sprintflow.backend.entity.Task;
import com.sprintflow.backend.entity.User;
import com.sprintflow.backend.enums.TaskPriority;
import com.sprintflow.backend.enums.TaskStatus;
import com.sprintflow.backend.repository.BoardRepository;
import com.sprintflow.backend.repository.ProjectMemberRepository;
import com.sprintflow.backend.repository.TaskRepository;
import com.sprintflow.backend.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;
    private final ProjectMemberRepository projectMemberRepository;

    public TaskService(
            TaskRepository taskRepository,
            BoardRepository boardRepository,
            UserRepository userRepository,
            ProjectMemberRepository projectMemberRepository) {

        this.taskRepository = taskRepository;
        this.boardRepository = boardRepository;
        this.userRepository = userRepository;
        this.projectMemberRepository = projectMemberRepository;
    }

    @Transactional
    public TaskResponse createTask(
            UUID boardId,
            CreateTaskRequest request,
            Authentication authentication) {

        Board board = boardRepository.findById(boardId)
                .orElseThrow(() ->
                        new RuntimeException("Board not found"));

        User currentUser = userRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new RuntimeException("User not found"));


        projectMemberRepository
                .findByUserAndProject(
                        currentUser,
                        board.getProject()
                )
                .orElseThrow(() ->
                        new RuntimeException(
                                "You are not a member of this project"));

        Task task = new Task();

        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setDueDate(request.getDueDate());
        task.setBoard(board);
        task.setCreatedBy(currentUser);

        if (request.getAssigneeId() != null) {

            User assignee = userRepository
                    .findById(request.getAssigneeId())
                    .orElseThrow(() ->
                            new RuntimeException("Assignee not found"));


            projectMemberRepository
                    .findByUserAndProject(
                            assignee,
                            board.getProject()
                    )
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "Assignee is not a member of this project"));

            task.setAssignee(assignee);
        }

        Task savedTask = taskRepository.save(task);

        return toResponse(savedTask);
    }

    private TaskResponse toResponse(Task task) {

        TaskResponse response = new TaskResponse();

        response.setId(task.getId());
        response.setTitle(task.getTitle());
        response.setDescription(task.getDescription());
        response.setTaskPriority(task.getTaskPriority());
        response.setTaskStatus(task.getTaskStatus());
        response.setCreatedAt(task.getCreatedAt());
        response.setDueDate(task.getDueDate());
        response.setUpdatedAt(task.getUpdatedAt());

        response.setBoardId(task.getBoard().getId());

        if (task.getAssignee() != null) {
            response.setAssigneeId(task.getAssignee().getId());
            response.setAssigneeName(task.getAssignee().getName());
        }

        response.setCreatedById(task.getCreatedBy().getId());
        response.setCreatedByName(task.getCreatedBy().getName());

        return response;
    }

    public List<TaskResponse> getTasks(
            UUID boardId,
            Authentication authentication) {

        Board board = boardRepository.findById(boardId)
                .orElseThrow(() ->
                        new RuntimeException("Board not found"));

        User user = userRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        projectMemberRepository
                .findByUserAndProject(user, board.getProject())
                .orElseThrow(() ->
                        new RuntimeException(
                                "You are not a member of this project"));

        return taskRepository.findByBoard(board)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public TaskResponse getTask(
            UUID taskId,
            Authentication authentication) {

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() ->
                        new RuntimeException("Task not found"));

        User user = userRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        projectMemberRepository
                .findByUserAndProject(
                        user,
                        task.getBoard().getProject()
                )
                .orElseThrow(() ->
                        new RuntimeException(
                                "You are not a member of this project"));

        return toResponse(task);
    }

    @Transactional
    public TaskResponse updateTask(
            UUID taskId,
            UpdateTaskRequest request,
            Authentication authentication) {

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() ->
                        new RuntimeException("Task not found"));

        User currentUser = userRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        projectMemberRepository
                .findByUserAndProject(
                        currentUser,
                        task.getBoard().getProject()
                )
                .orElseThrow(() ->
                        new RuntimeException(
                                "You are not a member of this project"));

        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setTaskPriority(request.getTaskPriority());
        task.setTaskStatus(request.getTaskStatus());
        task.setDueDate(request.getDueDate());

        if (request.getAssigneeId() != null) {

            User assignee = userRepository
                    .findById(request.getAssigneeId())
                    .orElseThrow(() ->
                            new RuntimeException("Assignee not found"));

            projectMemberRepository
                    .findByUserAndProject(
                            assignee,
                            task.getBoard().getProject()
                    )
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "Assignee is not a member of this project"));

            task.setAssignee(assignee);

        } else {
            task.setAssignee(null);
        }

        return toResponse(taskRepository.save(task));
    }

    @Transactional
    public void deleteTask(
            UUID taskId,
            Authentication authentication) {

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() ->
                        new RuntimeException("Task not found"));

        User user = userRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        projectMemberRepository
                .findByUserAndProject(
                        user,
                        task.getBoard().getProject()
                )
                .orElseThrow(() ->
                        new RuntimeException(
                                "You are not a member of this project"));

        taskRepository.delete(task);
    }

    @Transactional
    public TaskResponse assignTask(
            UUID taskId,
            AssignTaskRequest request,
            Authentication authentication) {

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() ->
                        new RuntimeException("Task not found"));

        User currentUser = userRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        projectMemberRepository
                .findByUserAndProject(
                        currentUser,
                        task.getBoard().getProject()
                )
                .orElseThrow(() ->
                        new RuntimeException(
                                "You are not a member of this project"));

        User assignee = userRepository
                .findById(request.getAssigneeId())
                .orElseThrow(() ->
                        new RuntimeException("Assignee not found"));

        projectMemberRepository
                .findByUserAndProject(
                        assignee,
                        task.getBoard().getProject()
                )
                .orElseThrow(() ->
                        new RuntimeException(
                                "Assignee is not a member of this project"));

        task.setAssignee(assignee);

        return toResponse(taskRepository.save(task));
    }

    @Transactional
    public TaskResponse moveTask(
            UUID taskId,
            MoveTaskRequest request,
            Authentication authentication) {

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() ->
                        new RuntimeException("Task not found"));

        User currentUser = userRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        projectMemberRepository
                .findByUserAndProject(
                        currentUser,
                        task.getBoard().getProject()
                )
                .orElseThrow(() ->
                        new RuntimeException(
                                "You are not a member of this project"));

        Board newBoard = boardRepository.findById(request.getBoardId())
                .orElseThrow(() ->
                        new RuntimeException("Target board not found"));

        if (!newBoard.getProject().getId()
                .equals(task.getBoard().getProject().getId())) {

            throw new RuntimeException(
                    "Target board does not belong to this project");
        }


        task.setBoard(newBoard);


        switch (newBoard.getName().toUpperCase()) {

            case "TODO" ->
                    task.setTaskStatus(TaskStatus.TODO);

            case "IN PROGRESS" ->
                    task.setTaskStatus(TaskStatus.IN_PROGRESS);

            case "IN REVIEW" ->
                    task.setTaskStatus(TaskStatus.IN_REVIEW);

            case "DONE" ->
                    task.setTaskStatus(TaskStatus.DONE);

            default ->
                    throw new RuntimeException(
                            "Unknown board status: " + newBoard.getName());
        }

        return toResponse(taskRepository.save(task));
    }

    public List<TaskResponse> searchTasks(
            UUID boardId,
            TaskStatus status,
            TaskPriority priority,
            String title,
            Authentication authentication) {

        Board board = boardRepository.findById(boardId)
                .orElseThrow(() ->
                        new RuntimeException("Board not found"));

        User user = userRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        projectMemberRepository
                .findByUserAndProject(
                        user,
                        board.getProject()
                )
                .orElseThrow(() ->
                        new RuntimeException(
                                "You are not a member of this project"));

        List<Task> tasks;

        if (status != null) {

            tasks = taskRepository
                    .findByBoardAndTaskStatus(board, status);

        } else if (priority != null) {

            tasks = taskRepository
                    .findByBoardAndTaskPriority(board, priority);

        } else if (title != null && !title.isBlank()) {

            tasks = taskRepository
                    .findByBoardAndTitleContainingIgnoreCase(
                            board,
                            title
                    );

        } else {

            tasks = taskRepository.findByBoard(board);
        }

        return tasks.stream()
                .map(this::toResponse)
                .toList();
    }
}