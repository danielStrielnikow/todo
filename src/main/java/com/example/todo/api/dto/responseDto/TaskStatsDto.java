package com.example.todo.api.dto.responseDto;

public record TaskStatsDto(
        int total,
        int newCount, 
        int inProgressCount, 
        int doneCount
) {
}
