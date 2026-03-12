package com.example.todo.exception;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record ErrorResponse(
                int status, 
                String message, 
                String path,
                
                @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") 
                LocalDateTime time
) {
    public ErrorResponse(int status, String message, String path) {
        this(status, message, path, LocalDateTime.now());
    }
}
