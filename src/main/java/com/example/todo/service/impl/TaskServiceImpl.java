package com.example.todo.service.impl;

import com.example.todo.api.dto.requestDto.TaskRequestDto;
import com.example.todo.api.dto.responseDto.TaskResponseDto;
import com.example.todo.exception.InvalidStatusTransitionException;
import com.example.todo.exception.ResourceNotFoundException;
import com.example.todo.exception.TaskAlreadyCompletedException;
import com.example.todo.mapper.TaskMapper;
import com.example.todo.model.Task;
import com.example.todo.model.enums.TaskStatus;
import com.example.todo.repository.TaskRepository;
import com.example.todo.service.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskServiceImpl implements TaskService {
    
    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;
    
    
    @Override
    @Transactional
    public TaskResponseDto create(TaskRequestDto dto) {
        Task task = taskMapper.toEntity(dto);
        if (task.getStatus() == null) {
            task.setStatus(TaskStatus.NEW);
        }
        TaskResponseDto response = taskMapper.toResponse(taskRepository.save(task));
        log.info("Task created: {}", response);
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponseDto> findAll() {
        return taskRepository.findAll()
                .stream()
                .map(taskMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TaskResponseDto findById(UUID id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));
        return taskMapper.toResponse(task);
    }

    @Override
    @Transactional
    public TaskResponseDto update(UUID id, TaskRequestDto dto) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));
        
        
        if (task.getStatus() == TaskStatus.DONE) throw new TaskAlreadyCompletedException(id);
        
        if (dto.getStatus() != null) validateStatusTransition(task.getStatus(), dto.getStatus());

        taskMapper.updateEntity(dto, task);
        Task saved = taskRepository.save(task);
        log.info("Task updated: {}", id);
        return taskMapper.toResponse(saved);
    }

    private void validateStatusTransition(TaskStatus from, TaskStatus to) {
        boolean valid = switch (from) {
            case NEW -> to == TaskStatus.IN_PROGRESS;
            case IN_PROGRESS -> to == TaskStatus.DONE;
            case DONE -> false;
        };

        if (!valid) {
            throw new InvalidStatusTransitionException(from, to);
        }
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        if (!taskRepository.existsById(id)) {
            throw new ResourceNotFoundException("Task not found with id: " + id);
        }
        taskRepository.deleteById(id);
        log.info("Task deleted: {}", id);
    }
}
