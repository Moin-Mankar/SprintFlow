package com.sprintflow.backend.dto.board;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class ReorderBoardsRequest {

    @NotEmpty
    private List<UUID> boardIds;
}
