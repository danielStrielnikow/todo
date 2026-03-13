package com.example.todo.api.dto.requestDto;

import com.example.todo.model.enums.TaskStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Filters for task list")
public class TaskFilterRequest {

    @Schema(description = "Filter by status", example = "NEW", allowableValues = {"NEW", "IN_PROGRESS", "DONE"})
    private TaskStatus status;

    @Schema(description = "Search keyword in task title", example = "buy")
    private String keyword;
}
