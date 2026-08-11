package com.sprintflow.backend.controller;

import com.sprintflow.backend.dto.board.BoardResponse;
import com.sprintflow.backend.dto.board.CreateBoardRequest;
import com.sprintflow.backend.service.BoardService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/projects/{projectId}/boards")
@SecurityRequirement(name = "bearerAuth")
public class BoardController {

    private final BoardService boardService;

    public BoardController(BoardService boardService) {
        this.boardService = boardService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BoardResponse createBoard(
            @PathVariable UUID projectId,
            @Valid @RequestBody CreateBoardRequest request,
            Authentication authentication) {

        return boardService.createBoard(
                projectId,
                request,
                authentication
        );
    }

    @GetMapping
    public List<BoardResponse> getBoards(
            @PathVariable UUID projectId,
            Authentication authentication) {

        return boardService.getBoards(
                projectId,
                authentication
        );
    }
}