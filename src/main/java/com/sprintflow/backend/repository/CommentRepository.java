package com.sprintflow.backend.repository;

import com.sprintflow.backend.entity.Comment;
import com.sprintflow.backend.entity.Task;
import com.sprintflow.backend.entity.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CommentRepository extends JpaRepository<Comment, UUID> {

    List<Comment> findByTaskOrderByCreatedAtAsc(Task task);

    void deleteByTask_Board_Project_Workspace(Workspace workspace);

    void deleteByTask(Task task);
}