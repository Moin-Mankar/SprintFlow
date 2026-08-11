package com.sprintflow.backend.repository;

import com.sprintflow.backend.entity.Project;
import com.sprintflow.backend.entity.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProjectRepository extends JpaRepository<Project, UUID> {
    List<Project> findByWorkspace(Workspace workspace);

    long countByWorkspace(Workspace workspace);
}