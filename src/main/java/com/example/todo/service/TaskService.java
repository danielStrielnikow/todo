package com.example.todo.service;

import com.example.todo.dto.requestDto.TaskRequestDto;
import com.example.todo.dto.responseDto.TaskResponseDto;

import java.util.List;
import java.util.UUID;

public interface TaskService {

    TaskResponseDto create(TaskRequestDto dto);

    List<TaskResponseDto> findAll();

    TaskResponseDto findById(UUID id);

    TaskResponseDto update(UUID id, TaskRequestDto dto);

    void delete(UUID id);
}
