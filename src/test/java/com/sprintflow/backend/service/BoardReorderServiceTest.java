package com.sprintflow.backend.service;

import com.sprintflow.backend.dto.board.BoardResponse;
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
import com.sprintflow.backend.service.caching.ProjectBoardsCacheService;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Authorization and complete-set validation of BoardService.reorderBoards, plus the
 * position values handed to the repository and the projectBoards cache eviction.
 */
class BoardReorderServiceTest {

    private static final String EMAIL = "manager@sprintflow.test";
    private static final UUID PROJECT_ID = UUID.randomUUID();

    private ProjectRepository projectRepository;
    private ProjectMemberRepository projectMemberRepository;
    private BoardRepository boardRepository;
    private UserRepository userRepository;
    private ProjectBoardsCacheService projectBoardsCacheService;

    private BoardService service;
    private Project project;
    private User user;
    private Map<String, Board> boardsByName;

    @BeforeEach
    void setUp() {

        projectRepository = mock(ProjectRepository.class);
        projectMemberRepository = mock(ProjectMemberRepository.class);
        boardRepository = mock(BoardRepository.class);
        userRepository = mock(UserRepository.class);
        projectBoardsCacheService = mock(ProjectBoardsCacheService.class);

        service = new BoardService(
                boardRepository,
                projectRepository,
                projectMemberRepository,
                userRepository,
                projectBoardsCacheService
        );

        project = new Project();
        project.setId(PROJECT_ID);
        project.setName("Payments Platform");

        user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail(EMAIL);

        boardsByName = new LinkedHashMap<>();
        boardsByName.put("TODO", board("TODO", 0));
        boardsByName.put("ACTIVE", board("ACTIVE", 1));
        boardsByName.put("REVIEW", board("REVIEW", 2));
        boardsByName.put("DONE", board("DONE", 3));

        when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.of(project));
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(boardRepository.findByProject(project)).thenReturn(new ArrayList<>(boardsByName.values()));
        when(boardRepository.saveAll(anyList())).thenAnswer(call -> call.getArgument(0));
        memberWithRole(ProjectRole.MANAGER);
    }

    private Board board(String name, int position) {
        Board board = new Board();
        board.setId(UUID.randomUUID());
        board.setName(name);
        board.setPosition(position);
        board.setProject(project);
        return board;
    }

    private void memberWithRole(ProjectRole role) {
        ProjectMember member = new ProjectMember();
        member.setUser(user);
        member.setProject(project);
        member.setProjectRole(role);
        when(projectMemberRepository.findByUserAndProject(user, project))
                .thenReturn(Optional.of(member));
    }

    private Authentication authentication() {
        return new UsernamePasswordAuthenticationToken(EMAIL, null);
    }

    private List<UUID> ids(String... names) {
        List<UUID> ids = new ArrayList<>();
        for (String name : names) {
            ids.add(boardsByName.get(name).getId());
        }
        return ids;
    }

    private ReorderBoardsRequest request(String... names) {
        ReorderBoardsRequest request = new ReorderBoardsRequest();
        request.setBoardIds(ids(names));
        return request;
    }

    @Test
    void boardRetrievalReadsPositionsInOrder() {

        when(boardRepository.findByProjectOrderByPositionAsc(project))
                .thenReturn(new ArrayList<>(boardsByName.values()));

        ProjectBoardsCacheService readThroughCache =
                new ProjectBoardsCacheService(boardRepository);

        BoardService readingService = new BoardService(
                boardRepository,
                projectRepository,
                projectMemberRepository,
                userRepository,
                readThroughCache
        );

        List<BoardResponse> responses = readingService.getBoards(PROJECT_ID, authentication());

        verify(boardRepository).findByProjectOrderByPositionAsc(project);

        assertThat(responses)
                .extracting(BoardResponse::getName)
                .containsExactly("TODO", "ACTIVE", "REVIEW", "DONE");
    }

    @Test
    void managerReorderPersistsSubmittedPositionsAndReturnsNewOrder() {

        List<BoardResponse> responses =
                service.reorderBoards(PROJECT_ID, request("TODO", "REVIEW", "ACTIVE", "DONE"), authentication());

        assertThat(responses)
                .extracting(BoardResponse::getName)
                .containsExactly("TODO", "REVIEW", "ACTIVE", "DONE");

        assertThat(responses)
                .extracting(BoardResponse::getPosition)
                .containsExactly(0, 1, 2, 3);

        assertThat(responses).allMatch(response -> response.getProjectId().equals(PROJECT_ID));

        ArgumentCaptor<List<Board>> saved = ArgumentCaptor.forClass(List.class);
        verify(boardRepository).saveAll(saved.capture());

        assertThat(saved.getValue())
                .extracting(Board::getName)
                .containsExactly("TODO", "REVIEW", "ACTIVE", "DONE");

        assertThat(saved.getValue())
                .extracting(Board::getPosition)
                .containsExactly(0, 1, 2, 3);
    }

    @Test
    void successfulReorderEvictsOnlyThatProjectsBoardCache() {

        service.reorderBoards(PROJECT_ID, request("DONE", "REVIEW", "ACTIVE", "TODO"), authentication());

        verify(projectBoardsCacheService).evictProjectBoards(PROJECT_ID);
    }

    @Test
    void submittingTheSameOrderStaysValidAndStable() {

        List<BoardResponse> responses =
                service.reorderBoards(PROJECT_ID, request("TODO", "ACTIVE", "REVIEW", "DONE"), authentication());

        assertThat(responses)
                .extracting(BoardResponse::getName, BoardResponse::getPosition)
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple("TODO", 0),
                        org.assertj.core.groups.Tuple.tuple("ACTIVE", 1),
                        org.assertj.core.groups.Tuple.tuple("REVIEW", 2),
                        org.assertj.core.groups.Tuple.tuple("DONE", 3));
    }

    @ParameterizedTest
    @EnumSource(value = ProjectRole.class, names = {"DEVELOPER", "TESTER", "VIEWER"})
    void nonManagementProjectRolesCannotReorder(ProjectRole role) {

        memberWithRole(role);

        assertThatThrownBy(() ->
                service.reorderBoards(PROJECT_ID, request("DONE", "REVIEW", "ACTIVE", "TODO"), authentication()))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("Only the project owner or a manager can reorder boards");

        verify(boardRepository, never()).saveAll(anyList());
        verify(projectBoardsCacheService, never()).evictProjectBoards(any());
    }

    @Test
    void ownerRoleCanReorder() {

        memberWithRole(ProjectRole.OWNER);

        assertThat(service.reorderBoards(PROJECT_ID, request("REVIEW", "TODO", "DONE", "ACTIVE"), authentication()))
                .extracting(BoardResponse::getName)
                .containsExactly("REVIEW", "TODO", "DONE", "ACTIVE");
    }

    @Test
    void nonProjectMemberCannotReorder() {

        when(projectMemberRepository.findByUserAndProject(user, project)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                service.reorderBoards(PROJECT_ID, request("DONE", "TODO", "ACTIVE", "REVIEW"), authentication()))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("You are not a member of this project");

        verify(boardRepository, never()).saveAll(anyList());
    }

    @Test
    void unknownProjectFailsAsNotFound() {

        assertThatThrownBy(() ->
                service.reorderBoards(UUID.randomUUID(), request("TODO", "ACTIVE", "REVIEW", "DONE"), authentication()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Project not found");
    }

    @Test
    void unknownBoardIdIsRejected() {

        ReorderBoardsRequest request = new ReorderBoardsRequest();
        request.setBoardIds(List.of(UUID.randomUUID(), boardsByName.get("TODO").getId(),
                boardsByName.get("ACTIVE").getId(), boardsByName.get("REVIEW").getId()));

        assertThatThrownBy(() -> service.reorderBoards(PROJECT_ID, request, authentication()))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Every board must belong to this project");

        verify(boardRepository, never()).saveAll(anyList());
        verify(projectBoardsCacheService, never()).evictProjectBoards(any());
    }

    @Test
    void boardOfAnotherProjectIsRejected() {

        Board foreign = new Board();
        foreign.setId(UUID.randomUUID());
        foreign.setName("SOMEONE ELSES BOARD");
        foreign.setPosition(0);
        foreign.setProject(new Project());

        when(boardRepository.findByProject(project))
                .thenReturn(List.of(boardsByName.get("TODO"), boardsByName.get("ACTIVE"),
                        boardsByName.get("REVIEW"), foreign));

        ReorderBoardsRequest request = new ReorderBoardsRequest();
        request.setBoardIds(List.of(boardsByName.get("TODO").getId(), boardsByName.get("ACTIVE").getId(),
                boardsByName.get("REVIEW").getId(), boardsByName.get("DONE").getId()));

        assertThatThrownBy(() -> service.reorderBoards(PROJECT_ID, request, authentication()))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Every board must belong to this project");

        verify(boardRepository, never()).saveAll(anyList());
    }

    @Test
    void duplicateBoardIdIsRejected() {

        ReorderBoardsRequest request = new ReorderBoardsRequest();
        request.setBoardIds(List.of(boardsByName.get("TODO").getId(), boardsByName.get("TODO").getId(),
                boardsByName.get("ACTIVE").getId(), boardsByName.get("REVIEW").getId()));

        assertThatThrownBy(() -> service.reorderBoards(PROJECT_ID, request, authentication()))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Board ids must not be duplicated");

        verify(boardRepository, never()).saveAll(anyList());
    }

    @Test
    void incompleteBoardSetIsRejected() {

        assertThatThrownBy(() ->
                service.reorderBoards(PROJECT_ID, request("TODO", "ACTIVE", "REVIEW"), authentication()))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("All boards of the project must be submitted");

        verify(boardRepository, never()).saveAll(anyList());
    }

    @Test
    void emptyBoardListFailsBeanValidation() {

        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

        ReorderBoardsRequest request = new ReorderBoardsRequest();
        request.setBoardIds(List.of());

        assertThat(validator.validate(request))
                .singleElement()
                .satisfies(violation -> assertThat(violation.getPropertyPath()).hasToString("boardIds"));

        request.setBoardIds(null);

        assertThat(validator.validate(request)).isNotEmpty();
    }
}
