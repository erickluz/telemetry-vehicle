package org.erick.telemetryloadgeneratorservice.controller;

import java.util.Map;

import org.erick.telemetryloadgeneratorservice.service.InvalidLoadTestRequestException;
import org.erick.telemetryloadgeneratorservice.service.LoadTestNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class LoadTestExceptionHandler {

    @ExceptionHandler(LoadTestNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(LoadTestNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", exception.getMessage()));
    }

    @ExceptionHandler(InvalidLoadTestRequestException.class)
    public ResponseEntity<Map<String, String>> handleInvalidRequest(InvalidLoadTestRequestException exception) {
        return ResponseEntity.badRequest()
                .body(Map.of("error", exception.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .orElse("Requisicao invalida");

        return ResponseEntity.badRequest()
                .body(Map.of("error", message));
    }
}
