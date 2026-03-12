package com.example.todo.controller;

import com.example.todo.dto.requestDto.TaskRequestDto;
import com.example.todo.dto.responseDto.TaskResponseDto;
import com.example.todo.model.enums.TaskStatus;
import com.example.todo.service.TaskService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.engine.TestExecutionResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TaskController.class)
public class TaskController {
    
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
        requestDto.setStatus(TaskStatus.NEW);

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
        when(taskService.findAll()).thenReturn(Arrays.asList(responseDto));

        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }
    
    @Test
    void PUT_updateTask_shouldReturnUpdated() throws Exception {
        when(taskService.update(eq(taskId), any())).thenReturn(responseDto);
        
        mockMvc.perform(put("api/tasks/{id}", taskId)
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
}
