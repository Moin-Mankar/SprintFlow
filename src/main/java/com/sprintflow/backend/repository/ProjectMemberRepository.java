package com.sprintflow.backend.repository;

import com.sprintflow.backend.entity.ProjectMember;
import com.sprintflow.backend.entity.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProjectMemberRepository extends JpaRepository<ProjectMember, UUID> {

}