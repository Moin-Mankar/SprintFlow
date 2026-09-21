package com.sprintflow.backend.specification;

import com.sprintflow.backend.entity.Board;
import com.sprintflow.backend.entity.Task;
import com.sprintflow.backend.enums.TaskPriority;
import com.sprintflow.backend.enums.TaskStatus;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.UUID;

public class TaskSpecification {

    public static Specification<Task> hasBoard(Board board){
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("board"),board);
    }

    public static Specification<Task> hasStatus(TaskStatus status){
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("taskStatus"),status);
    }

    public static Specification<Task> hasPriority(TaskPriority priority){
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("taskPriority"),priority);
    }

    public static Specification<Task> hasAssignee (UUID assigneeId){
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("assignee").get("id"), assigneeId);
    }

    public static Specification<Task>  titleContains(String title){
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(criteriaBuilder.lower(root.get("title")),
                        "%" + title.toLowerCase() + "%" );
    }

    public static Specification<Task> dueDateGreaterThanOrEqual(LocalDateTime localDateTime){
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.greaterThanOrEqualTo(root.get("dueDate"), localDateTime);
    }

    public static Specification<Task> dueDateLessThanOrEqual(LocalDateTime localDateTime){
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.lessThanOrEqualTo(root.get("dueDate"), localDateTime);
    }
}
