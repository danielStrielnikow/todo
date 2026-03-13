package com.example.todo.api.dto.responseDto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Task statistics")
public record TaskStatsDto(
        @Schema(description = "Total number of tasks") 
        int total,
        
        @Schema(description = "Number of tasks with status NEW") 
        int newCount,
        
        @Schema(description = "Number of tasks with status IN_PROGRESS") 
        int inProgressCount,
        
        @Schema(description = "Number of tasks with status DONE") 
        int doneCount
) {
}
