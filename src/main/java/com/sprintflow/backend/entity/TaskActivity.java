package com.sprintflow.backend.entity;

import com.sprintflow.backend.enums.TaskActivityType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.checkerframework.checker.units.qual.C;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Table(name = "task_activities")
@Entity
public class TaskActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskActivityType taskActivityType;

    @Column(nullable = false, length = 500)
    private String description;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id" , nullable = false)
    private User user;

    @PrePersist
    public void onCreate(){
        this.createdAt=LocalDateTime.now();
    }
}
