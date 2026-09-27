package com.sprintflow.backend.repository;

import com.sprintflow.backend.entity.Invitation;
import com.sprintflow.backend.entity.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface InvitationRepository extends JpaRepository<Invitation, UUID> {
    Optional<Invitation> findByToken(String token);

    void deleteByWorkspace(Workspace workspace);

}