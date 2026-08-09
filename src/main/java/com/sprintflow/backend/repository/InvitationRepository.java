package com.sprintflow.backend.repository;

import com.sprintflow.backend.entity.Invitation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface InvitationRepository extends JpaRepository<Invitation, UUID> {
}