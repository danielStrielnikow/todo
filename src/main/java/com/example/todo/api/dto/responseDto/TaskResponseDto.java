package com.example.todo.api.dto.responseDto;

import com.example.todo.model.enums.TaskStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Task response object")
public record TaskResponseDto(

        @Schema(description = "Task identifier", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID id,

        @Schema(description = "Task title", example = "Buy apple")
        String title,

        @Schema(description = "Task description", example = "From the shop on city")
        String description,

        @Schema(description = "Current task status", example = "NEW")
        TaskStatus status,

        @Schema(description = "Date and time when task was created", example = "2026-03-13 12:00:00")
        LocalDateTime createdAt,

        @Schema(description = "Date and time of last update", example = "2026-03-13 14:00:00")
        LocalDateTime updatedAt
) {}
