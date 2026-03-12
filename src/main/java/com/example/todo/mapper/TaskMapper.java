package com.example.todo.mapper;

import com.example.todo.dto.requestDto.TaskRequestDto;
import com.example.todo.dto.responseDto.TaskResponseDto;
import com.example.todo.model.Task;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface TaskMapper {
    TaskResponseDto toResponse(Task task);

    Task toEntity(TaskRequestDto dto);

    void updateEntity(TaskRequestDto dto, @MappingTarget Task task);
}
