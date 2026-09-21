package com.sprintflow.backend.service.caching;

import com.sprintflow.backend.dto.board.BoardResponse;
import com.sprintflow.backend.entity.Board;
import com.sprintflow.backend.entity.Project;
import com.sprintflow.backend.repository.BoardRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ProjectBoardsCacheService {

    private final BoardRepository boardRepository;

    public ProjectBoardsCacheService(
            BoardRepository boardRepository) {

        this.boardRepository = boardRepository;
    }

    @Cacheable(
            value = "projectBoards",
            key = "#projectId"
    )
    public List<BoardResponse> getBoards(
            UUID projectId,
            Project project) {

        return boardRepository.findByProject(project)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @CacheEvict(
            value = "projectBoards",
            key = "#projectId"
    )
    public void evictProjectBoards(UUID projectId) {
    }

    private BoardResponse toResponse(Board board) {

        BoardResponse response = new BoardResponse();

        response.setId(board.getId());
        response.setName(board.getName());
        response.setPosition(board.getPosition());
        response.setProjectId(board.getProject().getId());
        response.setCreatedAt(board.getCreatedAt());
        response.setUpdatedAt(board.getUpdatedAt());

        return response;
    }
}