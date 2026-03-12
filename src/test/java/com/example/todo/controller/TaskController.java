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
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
}
