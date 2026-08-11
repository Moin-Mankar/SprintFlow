package com.sprintflow.backend.repository;

import com.sprintflow.backend.entity.Project;
import com.sprintflow.backend.entity.ProjectMember;
import com.sprintflow.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjectMemberRepository
        extends JpaRepository<ProjectMember, UUID> {

    List<ProjectMember> findByProject(Project project);

    Optional<ProjectMember> findByUserAndProject(
            User user,
            Project project
    );

    boolean existsByProjectAndUser(
            Project project,
            User user
    );

    void deleteByProject(Project project);
}