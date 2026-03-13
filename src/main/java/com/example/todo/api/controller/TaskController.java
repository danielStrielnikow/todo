package com.example.todo.api.controller;

import com.example.todo.api.dto.requestDto.StatusUpdateRequest;
import com.example.todo.api.dto.requestDto.TaskFilterRequest;
import com.example.todo.api.dto.requestDto.TaskRequestDto;
import com.example.todo.api.dto.responseDto.TaskResponseDto;
import com.example.todo.api.dto.responseDto.TaskStatsDto;
import com.example.todo.model.enums.TaskStatus;
import com.example.todo.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Pageable;
import java.util.UUID;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {
    
    private final TaskService taskService;


    @Operation(summary = "Get task statistics")
    @ApiResponse(responseCode = "200", description = "Statistics returned")
    @GetMapping("/stats")
    public ResponseEntity<TaskStatsDto> getStats() {
        return ResponseEntity.ok(taskService.getStats());
    }

    @Operation(summary = "Create a new task")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Task created"),
            @ApiResponse(responseCode = "400", description = "Validation error")
    })
    @PostMapping
    public ResponseEntity<TaskResponseDto> create(@Valid @RequestBody TaskRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(taskService.create(dto));
    }

    @Operation(summary = "Get all tasks with optional filtering and pagination")
    @ApiResponse(responseCode = "200", description = "Task list returned")
    @GetMapping
    public ResponseEntity<Page<TaskResponseDto>> findAll(
            @ModelAttribute TaskFilterRequest filter,
            @PageableDefault(size = 10, sort = "createdAt",
                    direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(taskService.findAll(filter, pageable));
    }

    @Operation(summary = "Get task by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Task found"),
            @ApiResponse(responseCode = "404", description = "Task not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<TaskResponseDto> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(taskService.findById(id));
    }

    @Operation(summary = "Update task title and description")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Task updated"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "404", description = "Task not found"),
            @ApiResponse(responseCode = "409", description = "Task already completed")
    })
    @PutMapping("/{id}")
    public ResponseEntity<TaskResponseDto> update(@PathVariable UUID id,
                                                  @Valid @RequestBody TaskRequestDto dto) {
        return ResponseEntity.ok(taskService.update(id, dto));
    }

    @Operation(summary = "Update task status")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status updated"),
            @ApiResponse(responseCode = "400", description = "Invalid status value"),
            @ApiResponse(responseCode = "404", description = "Task not found"),
            @ApiResponse(responseCode = "409", description = "Task already completed"),
            @ApiResponse(responseCode = "422", description = "Invalid status transition")
    })
    @PatchMapping("/{id}/status")
    public ResponseEntity<TaskResponseDto> updateStatus(@PathVariable UUID id,
                                                        @Valid @RequestBody StatusUpdateRequest request) {
        return ResponseEntity.ok(taskService.updateStatus(id, request.getStatus()));
    }

    @Operation(summary = "Delete task by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Task deleted"),
            @ApiResponse(responseCode = "404", description = "Task not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        taskService.delete(id);
        return ResponseEntity.noContent().build();
    }
    
    
}
