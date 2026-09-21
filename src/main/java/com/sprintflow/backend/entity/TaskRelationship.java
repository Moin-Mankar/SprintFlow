package com.sprintflow.backend.entity;

import com.sprintflow.backend.enums.TaskRelationshipType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(
        name = "task_relationships",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_task_relationship",
                        columnNames = {"source_task_id", "target_task_id", "type"}
                )
        }
)
public class TaskRelationship {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_task_id" , nullable = false)
    private Task sourceTask;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_task_id" , nullable = false)
    private Task targetTask;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskRelationshipType type;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate(){
        this.createdAt=LocalDateTime.now();
    }

}
