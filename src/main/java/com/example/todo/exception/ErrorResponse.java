package com.example.todo.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Error response returned when get fails")
public record ErrorResponse(
                @Schema(description = "HTTP status code", example = "404")
                int status,

                @Schema(description = "Error message", example = "Task not found with id: 123e4567-e89b-12d3-a456-426614174000")
                String message,

                @Schema(description = "Request path that caused the error", example = "/api/tasks/123e4567-e89b-12d3-a456-426614174000")
                String path,

                @Schema(description = "Date and time when error occurred", example = "2026-03-13 12:00:00")
                @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
                LocalDateTime time
) {
    public ErrorResponse(int status, String message, String path) {
        this(status, message, path, LocalDateTime.now());
    }
}
