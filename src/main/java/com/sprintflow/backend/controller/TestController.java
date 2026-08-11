package com.sprintflow.backend.controller;

import com.sprintflow.backend.dto.task.TaskResponse;
import com.sprintflow.backend.dto.task.UpdateTaskRequest;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
public class TestController {

    @GetMapping("/api/test")
    public String test() {
        return "Authenticated successfully";
    }


}