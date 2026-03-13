package com.example.todo.api.dto.requestDto;

import com.example.todo.model.enums.TaskStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Request to update task status")
public class StatusUpdateRequest {

    @NotNull(message = "Status cannot be null")
    @Schema(description = "New task status", example = "IN_PROGRESS", allowableValues = {"NEW", "IN_PROGRESS", "DONE"})
    private TaskStatus status;
}
