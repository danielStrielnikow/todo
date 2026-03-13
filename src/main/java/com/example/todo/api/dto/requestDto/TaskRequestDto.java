package com.example.todo.api.dto.requestDto;

import com.example.todo.model.enums.TaskStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Request object for creating or updating a task")
public class TaskRequestDto {

    @NotBlank(message = "Title cannot be blank")
    @Size(max = 255, message = "Title cannot exceed 255 characters")
    @Schema(description = "Task title", example = "Buy apple", requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    @Schema(description = "Optional task description", example = "From the shop on city")
    private String description;

    @Schema(description = "Task status", example = "NEW", allowableValues = {"NEW", "IN_PROGRESS", "DONE"})
    private TaskStatus status;
}
