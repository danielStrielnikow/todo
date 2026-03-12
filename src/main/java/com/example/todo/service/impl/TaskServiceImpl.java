package com.example.todo.service.impl;

import com.example.todo.dto.requestDto.TaskRequestDto;
import com.example.todo.dto.responseDto.TaskResponseDto;
import com.example.todo.service.TaskService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {
    
    
    @Override
    @Transactional
    public TaskResponseDto create(TaskRequestDto dto) {
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponseDto> findAll() {
        return List.of();
    }

    @Override
    @Transactional(readOnly = true)
    public TaskResponseDto findById(UUID id) {
        return null;
    }

    @Override
    @Transactional
    public TaskResponseDto update(UUID id, TaskRequestDto dto) {
        return null;
    }

    @Override
    @Transactional
    public void delete(UUID id) {

    }
}
