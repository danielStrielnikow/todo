package com.example.todo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class TaskAlreadyCompletedException extends RuntimeException {

    public TaskAlreadyCompletedException(Object id) {
        super("Task with id " + id + " is already completed and cannot be modified");
    }
}
