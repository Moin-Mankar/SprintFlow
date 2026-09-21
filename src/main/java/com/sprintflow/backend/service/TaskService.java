package com.sprintflow.backend.service;

import com.sprintflow.backend.dto.task.*;
import com.sprintflow.backend.dto.websocket.ProjectEvent;
import com.sprintflow.backend.entity.Board;
import com.sprintflow.backend.entity.Task;
import com.sprintflow.backend.entity.TaskRelationship;
import com.sprintflow.backend.entity.User;
import com.sprintflow.backend.enums.TaskActivityType;
import com.sprintflow.backend.enums.TaskPriority;
import com.sprintflow.backend.enums.TaskStatus;
import com.sprintflow.backend.repository.*;
import com.sprintflow.backend.service.caching.ProjectDashboardCacheService;
import com.sprintflow.backend.specification.TaskSpecification;
import com.sprintflow.backend.service.websocket.WebSocketEventService;
import com.sprintflow.backend.exception.BadRequestException;
import com.sprintflow.backend.exception.ForbiddenException;
import com.sprintflow.backend.exception.ResourceNotFoundException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final NotificationService notificationService;
    private final TaskActivityService taskActivityService;
    private final TaskRelationshipRepository taskRelationshipRepository;
    private final ProjectDashboardCacheService projectDashboardCacheService;
    private final WebSocketEventService webSocketEventService;

    public TaskService(
            TaskRepository taskRepository,
            BoardRepository boardRepository,
            UserRepository userRepository,
            ProjectMemberRepository projectMemberRepository,
            NotificationService notificationService,
            TaskActivityService taskActivityService,
            TaskRelationshipRepository taskRelationshipRepository,
            ProjectDashboardCacheService projectDashboardCacheService,
            WebSocketEventService webSocketEventService) {

        this.taskRepository = taskRepository;
        this.boardRepository = boardRepository;
        this.userRepository = userRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.notificationService = notificationService;
        this.taskActivityService = taskActivityService;
        this.taskRelationshipRepository = taskRelationshipRepository;
        this.projectDashboardCacheService = projectDashboardCacheService;
        this.webSocketEventService = webSocketEventService;
    }

    @Transactional
    public TaskResponse createTask(
            UUID boardId,
            CreateTaskRequest request,
            Authentication authentication) {

        Board board = boardRepository.findById(boardId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Board not found"));

        User currentUser = userRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        projectMemberRepository
                .findByUserAndProject(
                        currentUser,
                        board.getProject()
                )
                .orElseThrow(() ->
                        new ForbiddenException(
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
                            new ResourceNotFoundException("Assignee not found"));

            projectMemberRepository
                    .findByUserAndProject(
                            assignee,
                            board.getProject()
                    )
                    .orElseThrow(() ->
                            new ForbiddenException(
                                    "Assignee is not a member of this project"));

            task.setAssignee(assignee);
        }

        Task savedTask = taskRepository.save(task);

        taskActivityService.recordActivity(
                currentUser,
                savedTask,
                TaskActivityType.CREATED,
                "Created the task: " + savedTask.getTitle()
        );

        projectDashboardCacheService.evictDashboard(
                board.getProject().getId()
        );

        ProjectEvent event = new ProjectEvent();

        event.setType("TASK_CREATED");
        event.setProjectId(board.getProject().getId());
        event.setTaskId(savedTask.getId());
        event.setMessage(
                "Task created: " + savedTask.getTitle()
        );

        webSocketEventService.sendProjectEvent(
                board.getProject().getId(),
                event
        );

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
                        new ResourceNotFoundException("Board not found"));

        User user = userRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        projectMemberRepository
                .findByUserAndProject(
                        user,
                        board.getProject()
                )
                .orElseThrow(() ->
                        new ForbiddenException(
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
                        new ResourceNotFoundException("Task not found"));

        User user = userRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        projectMemberRepository
                .findByUserAndProject(
                        user,
                        task.getBoard().getProject()
                )
                .orElseThrow(() ->
                        new ForbiddenException(
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
                        new ResourceNotFoundException("Task not found"));

        TaskPriority oldPriority = task.getTaskPriority();
        TaskStatus oldStatus = task.getTaskStatus();
        User oldAssignee = task.getAssignee();

        String oldTitle = task.getTitle();
        String oldDescription = task.getDescription();
        LocalDateTime oldDueDate = task.getDueDate();

        User currentUser = userRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        projectMemberRepository
                .findByUserAndProject(
                        currentUser,
                        task.getBoard().getProject()
                )
                .orElseThrow(() ->
                        new ForbiddenException(
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
                            new ResourceNotFoundException("Assignee not found"));

            projectMemberRepository
                    .findByUserAndProject(
                            assignee,
                            task.getBoard().getProject()
                    )
                    .orElseThrow(() ->
                            new ForbiddenException(
                                    "Assignee is not a member of this project"));

            task.setAssignee(assignee);

        } else {
            task.setAssignee(null);
        }

        Task savedTask = taskRepository.save(task);

        if (oldPriority != savedTask.getTaskPriority()) {

            taskActivityService.recordActivity(
                    currentUser,
                    savedTask,
                    TaskActivityType.UPDATED,
                    "Changed priority from " + oldPriority
                            + " to " + savedTask.getTaskPriority()
            );
        }

        if (oldStatus != savedTask.getTaskStatus()) {

            taskActivityService.recordActivity(
                    currentUser,
                    savedTask,
                    TaskActivityType.UPDATED,
                    "Changed status from " + oldStatus
                            + " to " + savedTask.getTaskStatus()
            );
        }

        UUID oldAssigneeId =
                oldAssignee != null ? oldAssignee.getId() : null;

        UUID newAssigneeId =
                savedTask.getAssignee() != null
                        ? savedTask.getAssignee().getId()
                        : null;

        if (!java.util.Objects.equals(oldAssigneeId, newAssigneeId)) {

            String description;

            if (savedTask.getAssignee() == null) {
                description = "Removed the task assignee";
            } else {
                description = "Assigned the task to "
                        + savedTask.getAssignee().getName();
            }

            taskActivityService.recordActivity(
                    currentUser,
                    savedTask,
                    TaskActivityType.ASSIGNED,
                    description
            );
        }

        if (!java.util.Objects.equals(oldTitle, savedTask.getTitle())) {

            taskActivityService.recordActivity(
                    currentUser,
                    savedTask,
                    TaskActivityType.UPDATED,
                    "Changed the task title"
            );
        }

        if (!java.util.Objects.equals(
                oldDescription,
                savedTask.getDescription())) {

            taskActivityService.recordActivity(
                    currentUser,
                    savedTask,
                    TaskActivityType.UPDATED,
                    "Updated the task description"
            );
        }

        if (!java.util.Objects.equals(
                oldDueDate,
                savedTask.getDueDate())) {

            taskActivityService.recordActivity(
                    currentUser,
                    savedTask,
                    TaskActivityType.UPDATED,
                    "Changed the task due date"
            );
        }

        projectDashboardCacheService.evictDashboard(
                task.getBoard().getProject().getId()
        );

        ProjectEvent event = new ProjectEvent();

        event.setType("TASK_UPDATED");
        event.setProjectId(task.getBoard().getProject().getId());
        event.setTaskId(savedTask.getId());
        event.setMessage(
                "Task updated: " + savedTask.getTitle()
        );

        webSocketEventService.sendProjectEvent(
                task.getBoard().getProject().getId(),
                event
        );

        return toResponse(savedTask);
    }

    @Transactional
    public void deleteTask(
            UUID taskId,
            Authentication authentication) {

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Task not found"));

        User user = userRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        projectMemberRepository
                .findByUserAndProject(
                        user,
                        task.getBoard().getProject()
                )
                .orElseThrow(() ->
                        new ForbiddenException(
                                "You are not a member of this project"));

        UUID projectId = task.getBoard().getProject().getId();

        taskRepository.delete(task);

        projectDashboardCacheService.evictDashboard(projectId);
    }

    @Transactional
    public TaskResponse assignTask(
            UUID taskId,
            AssignTaskRequest request,
            Authentication authentication) {

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Task not found"));

        User currentUser = userRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        projectMemberRepository
                .findByUserAndProject(
                        currentUser,
                        task.getBoard().getProject()
                )
                .orElseThrow(() ->
                        new ForbiddenException(
                                "You are not a member of this project"));

        User assignee = userRepository
                .findById(request.getAssigneeId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Assignee not found"));

        projectMemberRepository
                .findByUserAndProject(
                        assignee,
                        task.getBoard().getProject()
                )
                .orElseThrow(() ->
                        new ForbiddenException(
                                "Assignee is not a member of this project"));

        task.setAssignee(assignee);

        Task savedTask = taskRepository.save(task);

        taskActivityService.recordActivity(
                currentUser,
                savedTask,
                TaskActivityType.ASSIGNED,
                "Assigned the task to " + assignee.getName()
        );

        projectDashboardCacheService.evictDashboard(
                task.getBoard().getProject().getId()
        );

        ProjectEvent event = new ProjectEvent();

        event.setType("TASK_ASSIGNED");
        event.setProjectId(task.getBoard().getProject().getId());
        event.setTaskId(savedTask.getId());
        event.setMessage(
                "Task assigned to " + assignee.getName()
        );

        webSocketEventService.sendProjectEvent(
                task.getBoard().getProject().getId(),
                event
        );

        notificationService.sendNotification(
                assignee,
                "You were assigned the task: " + task.getTitle()
        );

        return toResponse(savedTask);
    }

    @Transactional
    public TaskResponse moveTask(
            UUID taskId,
            MoveTaskRequest request,
            Authentication authentication) {

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Task not found"));

        Board oldBoard = task.getBoard();
        TaskStatus oldStatus = task.getTaskStatus();

        User currentUser = userRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        projectMemberRepository
                .findByUserAndProject(
                        currentUser,
                        task.getBoard().getProject()
                )
                .orElseThrow(() ->
                        new ForbiddenException(
                                "You are not a member of this project"));

        Board newBoard = boardRepository.findById(request.getBoardId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Target board not found"));

        if (!newBoard.getProject().getId()
                .equals(task.getBoard().getProject().getId())) {

            throw new BadRequestException(
                    "Target board does not belong to this project");
        }

        TaskStatus newStatus;

        try {
            newStatus = TaskStatus.valueOf(
                    newBoard.getName().toUpperCase()
            );
        } catch (IllegalArgumentException e) {

            throw new BadRequestException(
                    "Unknown board status: " + newBoard.getName());
        }

        if (newStatus != TaskStatus.TODO) {

            List<TaskRelationship> blockingRelationships =
                    taskRelationshipRepository.findByTargetTask(task);

            for (TaskRelationship relationship : blockingRelationships) {

                Task blockingTask = relationship.getSourceTask();

                if (blockingTask.getTaskStatus() != TaskStatus.DONE) {

                    throw new BadRequestException(
                            "Task is blocked by: "
                                    + blockingTask.getTitle());
                }
            }
        }

        task.setBoard(newBoard);
        task.setTaskStatus(newStatus);

        Task savedTask = taskRepository.save(task);

        taskActivityService.recordActivity(
                currentUser,
                savedTask,
                TaskActivityType.MOVED,
                "Moved the task from " + oldBoard.getName()
                        + " to " + newBoard.getName()
        );

        projectDashboardCacheService.evictDashboard(
                oldBoard.getProject().getId()
        );

        ProjectEvent event = new ProjectEvent();

        event.setType("TASK_MOVED");
        event.setProjectId(oldBoard.getProject().getId());
        event.setTaskId(savedTask.getId());
        event.setMessage(
                "Task moved from "
                        + oldBoard.getName()
                        + " to "
                        + newBoard.getName()
        );

        webSocketEventService.sendProjectEvent(
                oldBoard.getProject().getId(),
                event
        );

        return toResponse(savedTask);
    }

    public Page<TaskResponse> searchTasks(
            UUID boardId,
            TaskStatus status,
            TaskPriority priority,
            UUID assigneeId,
            String title,
            LocalDateTime dueDateFrom,
            LocalDateTime dueDateTo,
            Pageable pageable,
            Authentication authentication) {

        Board board = boardRepository.findById(boardId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Board not found"));

        User user = userRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        projectMemberRepository
                .findByUserAndProject(
                        user,
                        board.getProject()
                )
                .orElseThrow(() ->
                        new ForbiddenException(
                                "You are not a member of this project"));

        Specification<Task> specification =
                TaskSpecification.hasBoard(board);

        if (status != null) {
            specification =
                    specification.and(TaskSpecification.hasStatus(status));
        }

        if (priority != null) {
            specification =
                    specification.and(TaskSpecification.hasPriority(priority));
        }

        if (assigneeId != null) {
            specification =
                    specification.and(TaskSpecification.hasAssignee(assigneeId));
        }

        if (title != null && !title.isBlank()) {
            specification =
                    specification.and(TaskSpecification.titleContains(title));
        }

        if (dueDateFrom != null) {
            specification =
                    specification.and(
                            TaskSpecification.dueDateGreaterThanOrEqual(
                                    dueDateFrom
                            )
                    );
        }

        if (dueDateTo != null) {
            specification =
                    specification.and(
                            TaskSpecification.dueDateLessThanOrEqual(
                                    dueDateTo
                            )
                    );
        }

        return taskRepository
                .findAll(specification, pageable)
                .map(this::toResponse);
    }
}