package com.example.todo.api.dto.requestDto;

import com.example.todo.model.enums.TaskStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskFilterRequest {

    private TaskStatus status;
    private String keyword;
}
