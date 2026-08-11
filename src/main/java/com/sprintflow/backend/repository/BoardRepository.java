package com.sprintflow.backend.repository;

import com.sprintflow.backend.entity.Board;
import com.sprintflow.backend.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BoardRepository extends JpaRepository<Board, UUID> {

    List<Board> findByProject(Project project);
}