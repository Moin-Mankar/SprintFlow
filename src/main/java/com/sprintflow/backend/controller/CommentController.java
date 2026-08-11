package com.sprintflow.backend.controller;

import com.sprintflow.backend.dto.comment.CommentResponse;
import com.sprintflow.backend.dto.comment.CreateCommentRequest;
import com.sprintflow.backend.service.CommentService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tasks/{taskId}/comments")
@SecurityRequirement(name = "bearerAuth")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CommentResponse createComment(
            @PathVariable UUID taskId,
            @Valid @RequestBody CreateCommentRequest request,
            Authentication authentication) {

        return commentService.createComment(
                taskId,
                request,
                authentication
        );
    }

    @GetMapping
    public List<CommentResponse> getComments(
            @PathVariable UUID taskId,
            Authentication authentication) {

        return commentService.getComments(
                taskId,
                authentication
        );
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(
            @PathVariable UUID commentId,
            Authentication authentication) {

        commentService.deleteComment(
                commentId,
                authentication
        );
    }
}