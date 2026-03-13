package com.example.todo.api.dto.requestDto;

import com.example.todo.model.enums.TaskStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StatusUpdateRequest {
    
    @NotNull(message = "Status cannot be null")
    private TaskStatus status;
}
