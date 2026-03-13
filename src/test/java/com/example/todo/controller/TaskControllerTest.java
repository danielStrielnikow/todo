package com.example.todo.controller;

import com.example.todo.api.controller.TaskController;
import com.example.todo.api.dto.requestDto.TaskFilterRequest;
import com.example.todo.api.dto.requestDto.TaskRequestDto;
import com.example.todo.api.dto.responseDto.TaskResponseDto;
import com.example.todo.api.dto.responseDto.TaskStatsDto;
import com.example.todo.exception.InvalidStatusTransitionException;
import com.example.todo.exception.ResourceNotFoundException;
import com.example.todo.exception.TaskAlreadyCompletedException;
import com.example.todo.model.enums.TaskStatus;
import com.example.todo.service.TaskService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TaskController.class)
public class TaskControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper  objectMapper;
    
    @MockBean
    private TaskService taskService;

    private UUID taskId;
    private TaskResponseDto responseDto;
    private TaskRequestDto requestDto;

    @BeforeEach
    void setUp() {
        taskId = UUID.randomUUID();

        requestDto = new TaskRequestDto();
        requestDto.setTitle("Test task");
        requestDto.setDescription("Test description");

        responseDto = new TaskResponseDto(
                taskId,
                "Test task",
                "Test description",
                TaskStatus.NEW,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }
    
    
    @Test
    void POST_createTask_shouldReturnCreated() throws Exception {
        when(taskService.create(any())).thenReturn(responseDto);
        
        mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Test task"))
                .andExpect(jsonPath("$.status").value("NEW"));
    }

    @Test
    
    void GET_getAllTasks_shouldReturnAllTasks() throws Exception {
        Page<TaskResponseDto> page = new PageImpl<>(List.of(responseDto));
        when(taskService.findAll(any(TaskFilterRequest.class), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }
    
    @Test
    void PUT_updateTask_shouldReturnUpdated() throws Exception {
        when(taskService.update(eq(taskId), any())).thenReturn(responseDto);
        
        mockMvc.perform(put("/api/tasks/{id}", taskId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test task"));
    }
    
    
    @Test
    void DELETE_task_shouldReturnDeleted() throws Exception {
        doNothing().when(taskService).delete(eq(taskId));
        
        mockMvc.perform(delete("/api/tasks/{id}", taskId))
                .andExpect(status().isNoContent());
    }
    
    @Test
    void POST_createTask_shouldReturn400_whenTitleIsBland() throws Exception {
        requestDto.setTitle("");
        
        mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
    }
    
    
    @Test
    void GET_getTaskById_shouldReturn404_whenNotFound() throws Exception {
        when(taskService.findById(taskId))
                .thenThrow(new ResourceNotFoundException("Task not found with id:" + taskId));

        mockMvc.perform(get("/api/tasks/{id}", taskId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.time").exists());
    }
    
    @Test
    void PUT_updateTask_shouldReturn409_whenTaskAlreadyDone() throws Exception {
        when(taskService.update(eq(taskId), any()))
                .thenThrow(new TaskAlreadyCompletedException(taskId));

        mockMvc.perform(put("/api/tasks/{id}", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").exists());
    }
    
    @Test
    void PUT_updateTask_shouldReturn422_whenStatusTransitionInvalid() throws Exception {
        when(taskService.update(eq(taskId), any()))
                .thenThrow(new InvalidStatusTransitionException(TaskStatus.DONE, TaskStatus.NEW));

        mockMvc.perform(put("/api/tasks/{id}", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.message").exists());
    }


    @Test
    void GET_getTaskById_shouldReturn400_whenUuidIsInvalid() throws Exception {
        mockMvc.perform(get("/api/tasks/not-good-uuid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }
    
    @Test
    void POST_createTask_shouldReturn400_whenInvalidEnumValue() throws Exception {
        String invalidJSON = """
                {
                    "title": invalid,
                    "status": INVALID,
                }
                """;

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void GET_getAllTasks_shouldReturnPagedResult() throws Exception {
        Page<TaskResponseDto> page = new PageImpl<>(List.of(responseDto));
        when(taskService.findAll(any(TaskFilterRequest.class), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/tasks")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "createdAt,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void GET_getAllTasks_shouldFilterByStatus() throws Exception {
        Page<TaskResponseDto> page = new PageImpl<>(List.of(responseDto));
        when(taskService.findAll(any(TaskFilterRequest.class), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/tasks")
                        .param("status", "NEW"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].status").value("NEW"));
    }

    @Test
    void GET_getAllTasks_shouldFilterByTitleKeyword() throws Exception {
        Page<TaskResponseDto> page = new PageImpl<>(List.of(responseDto));
        when(taskService.findAll(any(TaskFilterRequest.class), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/tasks")
                        .param("keyword", "Test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Test task"));
    }

    @Test
    void PATCH_updateStatus_shouldReturn200() throws Exception {
        when(taskService.updateStatus(eq(taskId), eq(TaskStatus.IN_PROGRESS)))
                .thenReturn(responseDto);

        mockMvc.perform(patch("/api/tasks/{id}/status", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": \"IN_PROGRESS\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("NEW"));
    }
    
    @Test
    void PATCH_updateStatus_shouldReturn400_whenStatusisNull() throws Exception {
        mockMvc.perform(patch("/api/tasks/{id}/status", taskId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\": null}"))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    void PATCH_updateStatus_shouldReturn409_whenTaskAlreadyDone() throws Exception {
        when(taskService.updateStatus(eq(taskId), any(TaskStatus.class)))
                .thenThrow(new TaskAlreadyCompletedException(taskId));
        
        mockMvc.perform(patch("/api/tasks/{id}/status", taskId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\": \"IN_PROGRESS\"}"))
                .andExpect(status().isConflict());
    }
    
    @Test
    void PATCH_updateStatus_shouldReturn422_whenTransitionInvalid() throws Exception {
        when(taskService.updateStatus(eq(taskId), any()))
                .thenThrow(new InvalidStatusTransitionException(TaskStatus.DONE, TaskStatus.NEW));
        
        mockMvc.perform(patch("/api/tasks/{id}/status", taskId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\": \"NEW\"}"))
                .andExpect(status().isUnprocessableEntity());
    }
    
    
    @Test
    void GET_stats_shouldReturn200_withStatistics() throws Exception {
        TaskStatsDto stats = new TaskStatsDto(10, 3, 5, 2);
        when(taskService.getStats()).thenReturn(stats);
        
        mockMvc.perform(get("/api/tasks/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(10))
                .andExpect(jsonPath("$.newCount").value(3))
                .andExpect(jsonPath("$.inProgressCount").value(5))
                .andExpect(jsonPath("$.doneCount").value(2));
    } 
    
    @Test
    void DELETE_task_shouldReturn409_whenTaskAlreadyDone() throws Exception {
        doThrow(new TaskAlreadyCompletedException(taskId))
                .when(taskService).delete(taskId);

        mockMvc.perform(delete("/api/tasks/{id}", taskId))
                .andExpect(status().isConflict());
    }
}
