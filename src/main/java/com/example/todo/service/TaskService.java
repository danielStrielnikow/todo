package com.example.todo.service;

import com.example.todo.api.dto.requestDto.TaskFilterRequest;
import com.example.todo.api.dto.requestDto.TaskRequestDto;
import com.example.todo.api.dto.responseDto.TaskResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface TaskService {

    TaskResponseDto create(TaskRequestDto dto);

    Page<TaskResponseDto> findAll(TaskFilterRequest filter, Pageable pageable);

    TaskResponseDto findById(UUID id);

    TaskResponseDto update(UUID id, TaskRequestDto dto);

    void delete(UUID id);
}
