package com.sprintflow.backend.entity;

import com.sprintflow.backend.enums.WorkspaceRole;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "workspace_members",
    uniqueConstraints = {
        @UniqueConstraint(
                name = "uk_workspace_member_user_workspace",
                columnNames = {"user_id", "workspace_id"}
        )
    }
    )
public class WorkspaceMember {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WorkspaceRole role;

    @Column(nullable = false)
    private LocalDateTime joinedAt;

    @JoinColumn(name = "user_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @JoinColumn(name = "workspace_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Workspace workspace;

    @PrePersist
    public void onCreate(){
        this.joinedAt=LocalDateTime.now();
    }
}
