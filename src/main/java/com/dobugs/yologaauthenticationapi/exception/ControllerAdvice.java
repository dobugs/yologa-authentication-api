package com.dobugs.yologaauthenticationapi.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.dobugs.yologaauthenticationapi.exception.dto.response.ExceptionResponse;

@RestControllerAdvice
public class ControllerAdvice {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionResponse> handleInternalServerError(Exception e) {
        final ExceptionResponse response = ExceptionResponse.from(e.getMessage());
        return ResponseEntity.internalServerError().body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ExceptionResponse> handleBadRequest(Exception e) {
        final ExceptionResponse response = ExceptionResponse.from(e.getMessage());
        return ResponseEntity.badRequest().body(response);
    }
}
