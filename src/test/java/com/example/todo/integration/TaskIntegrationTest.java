package com.example.todo.integration;

import com.example.todo.api.dto.requestDto.StatusUpdateRequest;
import com.example.todo.api.dto.requestDto.TaskRequestDto;
import com.example.todo.model.enums.TaskStatus;
import com.example.todo.repository.TaskRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TaskIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private TaskRepository taskRepository;


    private TaskRequestDto buildRequest(String title, String description) {
        TaskRequestDto dto = new TaskRequestDto();
        dto.setTitle(title);
        dto.setDescription(description);
        return dto;
    }

    @BeforeEach
    void cleanUp() {
        taskRepository.deleteAll();
    }
    
    
    @Test
    void shouldCreateAndRetrieveTask() throws Exception {
        TaskRequestDto request = buildRequest("Buy apple", "From the shop");

        MvcResult created = mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Buy apple"))
                .andExpect(jsonPath("$.status").value("NEW"))
                .andExpect(jsonPath("$.id").exists())
                .andReturn();
        
        String id = objectMapper.readTree(
                created.getResponse()
                        .getContentAsString())
                        .get("id")
                        .asText();

        mockMvc.perform(get("/api/tasks/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Buy apple"));
    }
    
    @Test
    void shouldUpdateTaskStatus_throughValidTransition() throws Exception {
        TaskRequestDto request = buildRequest("Write tests", null);

        MvcResult created = mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();
        
        String id = objectMapper.readTree(created.getResponse().getContentAsString())
                .get("id").asText();

        StatusUpdateRequest statusReqeust = new StatusUpdateRequest();
        statusReqeust.setStatus(TaskStatus.IN_PROGRESS);

        mockMvc.perform(patch("/api/tasks/{id}/status", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusReqeust)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    void shouldReturn404_whenTaskNotFound() throws Exception {
        mockMvc.perform(get("/api/tasks/00000000-0000-0000-0000-000000000000"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void shouldReturn400_whenTitleIsBlank() throws Exception {
        TaskRequestDto request = buildRequest("", null);

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldDeleteTask() throws Exception {
        TaskRequestDto request = buildRequest("Delete me", null);

        MvcResult created = mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        String id = objectMapper.readTree(created.getResponse().getContentAsString())
                .get("id").asText();

        mockMvc.perform(delete("/api/tasks/{id}", id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/tasks/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnStats() throws Exception {
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                buildRequest("Task 1", null))))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/tasks/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.newCount").value(1));
    }
    
    @Test
    void shouldReturn409_whenDeletingCompletedTask() throws Exception {
        MvcResult created = mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                buildRequest("Done task", null))))
                .andExpect(status().isCreated())
                .andReturn();
        
        String id = objectMapper.readTree(created.getResponse().getContentAsString())
                .get("id").asText();
        
        StatusUpdateRequest toInProgress = new StatusUpdateRequest();
        toInProgress.setStatus(TaskStatus.IN_PROGRESS);
        mockMvc.perform(patch("/api/tasks/{id}/status", id)
                .contentType(MediaType.APPLICATION_JSON)

                .content(objectMapper.writeValueAsString(toInProgress)));

        StatusUpdateRequest toDone = new StatusUpdateRequest();
        toDone.setStatus(TaskStatus.DONE);
        mockMvc.perform(patch("/api/tasks/{id}/status", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(toDone)));

        mockMvc.perform(delete("/api/tasks/{id}", id))
                .andExpect(status().isConflict());
    }
}
