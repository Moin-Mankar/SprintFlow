package com.sprintflow.backend.service;

import com.sprintflow.backend.dto.task.TaskRelationshipRequest;
import com.sprintflow.backend.dto.task.TaskRelationshipResponse;
import com.sprintflow.backend.entity.*;
import com.sprintflow.backend.enums.ProjectRole;
import com.sprintflow.backend.enums.TaskRelationshipType;
import com.sprintflow.backend.repository.ProjectMemberRepository;
import com.sprintflow.backend.repository.TaskRelationshipRepository;
import com.sprintflow.backend.repository.TaskRepository;
import com.sprintflow.backend.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import com.sprintflow.backend.service.caching.ProjectDashboardCacheService;

import java.util.List;
import java.util.UUID;

@Service
public class TaskRelationshipService {

    private final TaskRelationshipRepository taskRelationshipRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectDashboardCacheService projectDashboardCacheService;

    public TaskRelationshipService(TaskRelationshipRepository taskRelationshipRepository, TaskRepository taskRepository, UserRepository userRepository, ProjectMemberRepository projectMemberRepository, ProjectDashboardCacheService projectDashboardCacheService) {
        this.taskRelationshipRepository = taskRelationshipRepository;
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.projectDashboardCacheService = projectDashboardCacheService;
    }

    @Transactional
    public TaskRelationshipResponse createRelationship(TaskRelationshipRequest request, Authentication authentication){
        Task sourceTask = taskRepository.findById(request.getSourceTaskId()).orElseThrow(()->
                new RuntimeException("Task with id :" + request.getSourceTaskId() + " not found"));

        Task targetTask = taskRepository.findById(request.getTargetTaskId()).orElseThrow(()->
                new RuntimeException("Task with id :" + request.getSourceTaskId() + " not found"));

        if(sourceTask.getId().equals(targetTask.getId())){
            throw  new RuntimeException("A task cannot have relationship with itself");
        }

        if(!sourceTask.getBoard().getProject().getId().equals(targetTask.getBoard().getProject().getId())){
            throw  new RuntimeException("Tasks must belong to the same project");
        }

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        verifyRelationshipPermission(
                user,
                sourceTask.getBoard().getProject()
        );

        if(taskRelationshipRepository.existsBySourceTaskAndTargetTask(sourceTask, targetTask)){
            throw  new RuntimeException("This task relationship already exists");
        }

        TaskRelationship relationship = new TaskRelationship();
        relationship.setSourceTask(sourceTask);
        relationship.setTargetTask(targetTask);
        relationship.setType(TaskRelationshipType.BLOCKS);

        TaskRelationship savedRelationShip = taskRelationshipRepository.save(relationship);

        projectDashboardCacheService.evictDashboard(
                sourceTask.getBoard().getProject().getId()
        );

        return toRespone(savedRelationShip);

    }

    private TaskRelationshipResponse toRespone(TaskRelationship relationship) {

        TaskRelationshipResponse response = new TaskRelationshipResponse();

        response.setId(relationship.getId());
        response.setSourceTaskId(relationship.getSourceTask().getId());
        response.setSourceTaskTitle(relationship.getSourceTask().getTitle());
        response.setTargetTaskId(relationship.getTargetTask().getId());
        response.setTargetTaskTitle(relationship.getTargetTask().getTitle());
        response.setType(relationship.getType());
        response.setCreatedAt(relationship.getCreatedAt());

        return response;
    }

    public void verifyRelationshipPermission(User user , Project project){

        ProjectMember member = projectMemberRepository.findByUserAndProject(user, project)
                .orElseThrow(()->new RuntimeException(
                        "You are not a member of this project"));

        if(member.getProjectRole() != ProjectRole.OWNER && member.getProjectRole() !=ProjectRole.MANAGER){
            throw  new RuntimeException("Only project owners and managers can manage task relationships");
        }
    }

    @Transactional
    public void deleteRelationship(UUID relatioshipId, Authentication authentication){
        TaskRelationship relationship = taskRelationshipRepository.findById(relatioshipId).orElseThrow(() ->
                new RuntimeException(
                        "Task relationship not found"));

        User user = userRepository.findByEmail(authentication.getName()).orElseThrow(() ->
                new RuntimeException("User not found"));

        verifyRelationshipPermission(user , relationship.getSourceTask().getBoard().getProject());

        UUID projectId =
                relationship.getSourceTask()
                        .getBoard()
                        .getProject()
                        .getId();

        taskRelationshipRepository.delete(relationship);

        projectDashboardCacheService.evictDashboard(projectId);
    }

    public List<TaskRelationshipResponse> getRelationshipsForTask(
            UUID taskId,
            Authentication authentication) {

        Task task = getAuthorizedTask(
                taskId,
                authentication
        );

        List<TaskRelationship> outgoing =
                taskRelationshipRepository.findBySourceTask(task);

        List<TaskRelationship> incoming =
                taskRelationshipRepository.findByTargetTask(task);

        return java.util.stream.Stream
                .concat(
                        outgoing.stream(),
                        incoming.stream()
                )
                .map(this::toRespone)
                .toList();
    }

    private Task getAuthorizedTask(
            UUID taskId,
            Authentication authentication) {

        Task task = taskRepository
                .findById(taskId)
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

        return task;
    }
}
