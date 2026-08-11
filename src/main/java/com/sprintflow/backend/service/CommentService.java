package com.sprintflow.backend.service;

import com.sprintflow.backend.dto.comment.CommentResponse;
import com.sprintflow.backend.dto.comment.CreateCommentRequest;
import com.sprintflow.backend.entity.Comment;
import com.sprintflow.backend.entity.Task;
import com.sprintflow.backend.entity.User;
import com.sprintflow.backend.repository.CommentRepository;
import com.sprintflow.backend.repository.ProjectMemberRepository;
import com.sprintflow.backend.repository.TaskRepository;
import com.sprintflow.backend.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final ProjectMemberRepository projectMemberRepository;

    public CommentService(
            CommentRepository commentRepository,
            TaskRepository taskRepository,
            UserRepository userRepository,
            ProjectMemberRepository projectMemberRepository) {

        this.commentRepository = commentRepository;
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.projectMemberRepository = projectMemberRepository;
    }

    @Transactional
    public CommentResponse createComment(
            UUID taskId,
            CreateCommentRequest request,
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

        Comment comment = new Comment();

        comment.setContent(request.getContent());
        comment.setTask(task);
        comment.setUser(user);

        Comment savedComment = commentRepository.save(comment);

        return toResponse(savedComment);
    }

    private CommentResponse toResponse(Comment comment) {

        CommentResponse response = new CommentResponse();

        response.setId(comment.getId());
        response.setContent(comment.getContent());
        response.setCreatedAt(comment.getCreatedAt());

        response.setTaskId(comment.getTask().getId());

        response.setUserId(comment.getUser().getId());
        response.setUserName(comment.getUser().getName());

        return response;
    }

    public List<CommentResponse> getComments(
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

        return commentRepository
                .findByTaskOrderByCreatedAtAsc(task)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public void deleteComment(
            UUID commentId,
            Authentication authentication) {

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() ->
                        new RuntimeException("Comment not found"));

        User user = userRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        if (!comment.getUser().getId().equals(user.getId())) {
            throw new RuntimeException(
                    "You can only delete your own comments");
        }

        commentRepository.delete(comment);
    }
}