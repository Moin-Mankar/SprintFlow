package com.sprintflow.backend.repository;

import com.sprintflow.backend.entity.User;
import com.sprintflow.backend.entity.Workspace;
import com.sprintflow.backend.entity.WorkspaceMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WorkspaceMemberRepository
        extends JpaRepository<WorkspaceMember, UUID> {

    List<WorkspaceMember> findByUser(User user);

    Optional<WorkspaceMember> findByUserAndWorkspace(
            User user,
            Workspace workspace
    );

    void deleteByWorkspace(Workspace workspace);
}