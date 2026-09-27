package com.sprintflow.backend.service;

import com.sprintflow.backend.dto.board.BoardResponse;
import com.sprintflow.backend.dto.board.CreateBoardRequest;
import com.sprintflow.backend.dto.board.ReorderBoardsRequest;
import com.sprintflow.backend.entity.Board;
import com.sprintflow.backend.entity.Project;
import com.sprintflow.backend.entity.ProjectMember;
import com.sprintflow.backend.entity.User;
import com.sprintflow.backend.enums.ProjectRole;
import com.sprintflow.backend.exception.BadRequestException;
import com.sprintflow.backend.exception.ForbiddenException;
import com.sprintflow.backend.exception.ResourceNotFoundException;
import com.sprintflow.backend.repository.BoardRepository;
import com.sprintflow.backend.repository.ProjectMemberRepository;
import com.sprintflow.backend.repository.ProjectRepository;
import com.sprintflow.backend.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.sprintflow.backend.service.caching.ProjectBoardsCacheService;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class BoardService {

    private final BoardRepository boardRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;
    private final ProjectBoardsCacheService projectBoardsCacheService;

    public BoardService(
            BoardRepository boardRepository,
            ProjectRepository projectRepository,
            ProjectMemberRepository projectMemberRepository,
            UserRepository userRepository,
            ProjectBoardsCacheService projectBoardsCacheService) {

        this.boardRepository = boardRepository;
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.userRepository = userRepository;
        this.projectBoardsCacheService = projectBoardsCacheService;
    }

    @Transactional
    public BoardResponse createBoard(
            UUID projectId,
            CreateBoardRequest request,
            Authentication authentication) {

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Project not found"));

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        ProjectMember member = projectMemberRepository
                .findByUserAndProject(user, project)
                .orElseThrow(() ->
                        new ForbiddenException(
                                "You are not a member of this project"));

        if (member.getProjectRole() != ProjectRole.OWNER &&
                member.getProjectRole() != ProjectRole.MANAGER) {

            throw new ForbiddenException(
                    "Only the project owner or a manager can create boards");
        }

        Board board = new Board();

        board.setName(request.getName());
        board.setPosition(request.getPosition());
        board.setProject(project);

        Board savedBoard = boardRepository.save(board);

        projectBoardsCacheService.evictProjectBoards(
                projectId
        );

        return toResponse(savedBoard);
    }

    public List<BoardResponse> getBoards(
            UUID projectId,
            Authentication authentication) {

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Project not found"));

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        projectMemberRepository
                .findByUserAndProject(user, project)
                .orElseThrow(() ->
                        new ForbiddenException(
                                "You are not a member of this project"));

        return projectBoardsCacheService.getBoards(
                projectId,
                project
        );
    }

    @Transactional
    public List<BoardResponse> reorderBoards(
            UUID projectId,
            ReorderBoardsRequest request,
            Authentication authentication) {

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Project not found"));

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Authenticated user not found"));

        ProjectMember member = projectMemberRepository
                .findByUserAndProject(user, project)
                .orElseThrow(() ->
                        new ForbiddenException("You are not a member of this project"));

        if (member.getProjectRole() != ProjectRole.OWNER &&
                member.getProjectRole() != ProjectRole.MANAGER) {

            throw new ForbiddenException(
                    "Only the project owner or a manager can reorder boards");
        }

        List<UUID> boardIds = request.getBoardIds();

        if (boardIds.stream().distinct().count() != boardIds.size()) {
            throw new BadRequestException("Board ids must not be duplicated");
        }

        List<Board> projectBoards = boardRepository.findByProject(project);

        Map<UUID, Board> boardsById = projectBoards.stream()
                .collect(Collectors.toMap(Board::getId, Function.identity()));

        for (UUID boardId : boardIds) {
            if (!boardsById.containsKey(boardId)) {
                throw new BadRequestException(
                        "Every board must belong to this project");
            }
        }

        if (boardIds.size() != projectBoards.size()) {
            throw new BadRequestException(
                    "All boards of the project must be submitted");
        }

        for (int index = 0; index < boardIds.size(); index++) {
            boardsById.get(boardIds.get(index)).setPosition(index);
        }

        List<Board> reordered = boardRepository.saveAll(
                boardIds.stream()
                        .map(boardsById::get)
                        .toList()
        );

        projectBoardsCacheService.evictProjectBoards(
                projectId
        );

        return reordered.stream()
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