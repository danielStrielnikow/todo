package com.example.todo.api.dto.responseDto;

import com.example.todo.model.enums.TaskStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record TaskResponseDto(
        UUID id,
        String title,
        String description,
        TaskStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt  
) {
}
