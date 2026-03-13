package com.example.todo.service.impl;

import com.example.todo.api.dto.requestDto.TaskFilterRequest;
import com.example.todo.api.dto.requestDto.TaskRequestDto;
import com.example.todo.api.dto.responseDto.TaskResponseDto;
import com.example.todo.exception.InvalidStatusTransitionException;
import com.example.todo.exception.ResourceNotFoundException;
import com.example.todo.exception.TaskAlreadyCompletedException;
import com.example.todo.mapper.TaskMapper;
import com.example.todo.model.Task;
import com.example.todo.model.enums.TaskStatus;
import com.example.todo.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceImplTest {

    @Mock
    private TaskRepository taskRepository;
    
    @Mock
    private TaskMapper taskMapper;
    
    @InjectMocks
    private TaskServiceImpl taskService;
    
    private UUID taskId;
    private Task task;
    private TaskRequestDto requestDto;
    private TaskResponseDto responseDto;


    @BeforeEach
    void setUp() {
        taskId = UUID.randomUUID();

        task = new Task();
        task.setTitle("Test task");
        task.setDescription("Test description");
        task.setStatus(TaskStatus.NEW);

        requestDto = new TaskRequestDto();
        requestDto.setTitle("Test task");
        requestDto.setDescription("Test description");
        requestDto.setStatus(TaskStatus.NEW);

        responseDto = new TaskResponseDto(
                taskId,
                "Test task",
                "Test Description",
                TaskStatus.NEW,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }
    
    @Test
    void create_shouldReturnResponseDto_whenValidRequest() {
        when(taskMapper.toEntity(requestDto)).thenReturn(task);
        when(taskRepository.save(task)).thenReturn(task);
        when(taskMapper.toResponse(task)).thenReturn(responseDto);
        
        TaskResponseDto result = taskService.create(requestDto);

        assertThat(result).isNotNull();
        assertThat(result.title()).isEqualTo("Test task");
        assertThat(result.status()).isEqualTo(TaskStatus.NEW);
        verify(taskRepository).save(task);
    }
    
    @Test
    void create_shouldSetStatusNew_whenStatusNull() {
        requestDto.setStatus(null);
        task.setStatus(null);
        
        when(taskMapper.toEntity(requestDto)).thenReturn(task);
        when(taskRepository.save(task)).thenReturn(task);
        when(taskMapper.toResponse(task)).thenReturn(responseDto);
        
        taskService.create(requestDto);
        
        assertThat(task.getStatus()).isEqualTo(TaskStatus.NEW);
    }

    @Test
    void findAll_shouldReturnPagedTasks() {
        TaskFilterRequest filter = new TaskFilterRequest();
        Pageable pageable = PageRequest.of(0, 10);
        Page<Task> taskPage = new PageImpl<>(List.of(task));

        when(taskRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(taskPage);
        when(taskMapper.toResponse(task)).thenReturn(responseDto);

        Page<TaskResponseDto> result = taskService.findAll(filter, pageable);

        assertThat(result.getContent().size()).isEqualTo(1);
        assertThat(result.getContent().get(0).title()).isEqualTo("Test task");
    }

    @Test
    void findById_shouldReturnTask_whenExists() {
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(taskMapper.toResponse(task)).thenReturn(responseDto);

        TaskResponseDto result = taskService.findById(taskId);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(taskId);
    }

    @Test
    void findById_shouldThrowException_whenNotFound() {
        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.findById(taskId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(taskId.toString());
    }

    @Test
    void update_shouldReturnUpdatedTask_whenExists() {
        task.setStatus(TaskStatus.NEW);
        requestDto.setStatus(TaskStatus.IN_PROGRESS); 

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(taskRepository.save(task)).thenReturn(task);
        when(taskMapper.toResponse(task)).thenReturn(responseDto);

        TaskResponseDto result = taskService.update(taskId, requestDto);

        assertThat(result).isNotNull();
        verify(taskMapper).updateEntity(requestDto, task);
        verify(taskRepository).save(task);
    }

    @Test
    void update_shouldThrowException_whenNotFound() {
        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.update(taskId, requestDto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(taskId.toString());

        verify(taskRepository, never()).save(any());
    }

    @Test
    void delete_shouldDeleteTask_whenExists() {
        when(taskRepository.existsById(taskId)).thenReturn(true);

        taskService.delete(taskId);

        verify(taskRepository).deleteById(taskId);
    }

    @Test
    void delete_shouldThrowException_whenNotFound() {
        when(taskRepository.existsById(taskId)).thenReturn(false);

        assertThatThrownBy(() -> taskService.delete(taskId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(taskId.toString());

        verify(taskRepository, never()).deleteById(any());
    }

    @Test
    void update_shouldThrowException_whenTaskIsAlreadyDone() {
        task.setStatus(TaskStatus.DONE);
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

        assertThatThrownBy(() -> taskService.update(taskId, requestDto))
                .isInstanceOf(TaskAlreadyCompletedException.class);

        verify(taskRepository, never()).save(any());
    }

    @Test
    void update_shouldThrowException_whenStatusTransitionIsInvalid() {
        task.setStatus(TaskStatus.IN_PROGRESS);
        requestDto.setStatus(TaskStatus.NEW);
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

        assertThatThrownBy(() -> taskService.update(taskId, requestDto))
                .isInstanceOf(InvalidStatusTransitionException.class);

        verify(taskRepository, never()).save(any());
    }
    
    @Test
    void updateStatus_shouldReturnUpdatedTask_whenTransitionValid() {
        task.setStatus(TaskStatus.NEW);
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(taskRepository.save(task)).thenReturn(task);
        when(taskMapper.toResponse(task)).thenReturn(responseDto);
        
        TaskResponseDto result = taskService.updateStatus(taskId, TaskStatus.IN_PROGRESS);
        
        assertThat(result).isNotNull();
        verify(taskRepository).save(task);
    }
    
    @Test
    void updateStatus_shouldThrowException_whenTaskAlreadyDone() {
        task.setStatus(TaskStatus.DONE);
        
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        
        assertThatThrownBy(() -> taskService.updateStatus(taskId, TaskStatus.NEW))
                .isInstanceOf(TaskAlreadyCompletedException.class);
        
        verify(taskRepository, never()).save(any());
    }
    
    
    @Test
    void getStats_shouldReturnCorrectCount() {
        when(taskRepository.countByStatus(TaskStatus.NEW)).thenReturn(3);
        when(taskRepository.countByStatus(TaskStatus.IN_PROGRESS)).thenReturn(5);
        when(taskRepository.countByStatus(TaskStatus.DONE)).thenReturn(2);
        when(taskRepository.count()).thenReturn(10);
        
        TaskStatsDto result = taskService.getStats();

        assertThat(result.total()).isEqualTo(10);
        assertThat(result.newCount()).isEqualTo(3);
        assertThat(result.inProgressCount()).isEqualTo(5);
        assertThat(result.doneCount()).isEqualTo(2);
    }
}