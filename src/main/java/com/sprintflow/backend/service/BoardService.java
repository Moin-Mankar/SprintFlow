package com.sprintflow.backend.service;

import com.sprintflow.backend.dto.board.BoardResponse;
import com.sprintflow.backend.dto.board.CreateBoardRequest;
import com.sprintflow.backend.entity.Board;
import com.sprintflow.backend.entity.Project;
import com.sprintflow.backend.entity.User;
import com.sprintflow.backend.repository.BoardRepository;
import com.sprintflow.backend.repository.ProjectMemberRepository;
import com.sprintflow.backend.repository.ProjectRepository;
import com.sprintflow.backend.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class BoardService {

    private final BoardRepository boardRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;

    public BoardService(
            BoardRepository boardRepository,
            ProjectRepository projectRepository,
            ProjectMemberRepository projectMemberRepository,
            UserRepository userRepository) {

        this.boardRepository = boardRepository;
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public BoardResponse createBoard(
            UUID projectId,
            CreateBoardRequest request,
            Authentication authentication) {

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() ->
                        new RuntimeException("Project not found"));

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        projectMemberRepository
                .findByUserAndProject(user, project)
                .orElseThrow(() ->
                        new RuntimeException(
                                "You are not a member of this project"));

        Board board = new Board();

        board.setName(request.getName());
        board.setPosition(request.getPosition());
        board.setProject(project);

        Board savedBoard = boardRepository.save(board);

        return toResponse(savedBoard);
    }

    public List<BoardResponse> getBoards(
            UUID projectId,
            Authentication authentication) {

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() ->
                        new RuntimeException("Project not found"));

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        projectMemberRepository
                .findByUserAndProject(user, project)
                .orElseThrow(() ->
                        new RuntimeException(
                                "You are not a member of this project"));

        return boardRepository.findByProject(project)
                .stream()
                .map(this::toResponse)
                .toList();
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