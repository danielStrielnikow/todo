package com.example.todo.service.impl;

import com.example.todo.api.dto.requestDto.TaskFilterRequest;
import com.example.todo.api.dto.requestDto.TaskRequestDto;
import com.example.todo.api.dto.responseDto.TaskResponseDto;
import com.example.todo.api.dto.responseDto.TaskStatsDto;
import com.example.todo.exception.InvalidStatusTransitionException;
import com.example.todo.exception.ResourceNotFoundException;
import com.example.todo.exception.TaskAlreadyCompletedException;
import com.example.todo.mapper.TaskMapper;
import com.example.todo.model.Task;
import com.example.todo.model.enums.TaskStatus;
import com.example.todo.repository.TaskRepository;
import com.example.todo.repository.TaskSpecification;
import com.example.todo.service.TaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;


    @Override
    @Transactional(readOnly = true)
    public TaskStatsDto getStats() {
        int total = (int) taskRepository.count();
        int newCount = taskRepository.countByStatus(TaskStatus.NEW);
        int inProgress = taskRepository.countByStatus(TaskStatus.IN_PROGRESS);
        int done = taskRepository.countByStatus(TaskStatus.DONE);
        log.info("Stats: total={}, new={}, inProgress={}, done={}", total, newCount, inProgress, done);
        return new TaskStatsDto(total, newCount, inProgress, done);
    }

    @Override
    @Transactional
    public TaskResponseDto create(TaskRequestDto dto) {
        Task task = taskMapper.toEntity(dto);
        
        task.setStatus(TaskStatus.NEW);

        TaskResponseDto response = taskMapper.toResponse(taskRepository.save(task));
        log.info("Task created: {}", response);
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TaskResponseDto> findAll(TaskFilterRequest filter, Pageable pageable) {
        return taskRepository
                .findAll(TaskSpecification.withFilters(filter), pageable)
                .map(taskMapper::toResponse);
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

        taskMapper.updateEntity(dto, task);
        Task saved = taskRepository.save(task);
        log.info("Task updated: {}", id);
        return taskMapper.toResponse(saved);
    }

    @Override
    public TaskResponseDto updateStatus(UUID id, TaskStatus newStatus) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));

        if (task.getStatus() == TaskStatus.DONE) throw new TaskAlreadyCompletedException(id);

        validateStatusTransition(task.getStatus(), newStatus);
        task.setStatus(newStatus);
        Task savedStatus = taskRepository.save(task);
        log.info("Task updated status: {}", id);
        return taskMapper.toResponse(savedStatus);
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
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));

        if (task.getStatus() == TaskStatus.DONE) throw new TaskAlreadyCompletedException(id);

        taskRepository.deleteById(id);
        log.info("Task deleted: {}", id);
    }
}
