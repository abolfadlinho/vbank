package com.vbank.bffservice.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DownstreamServiceException.class)
    public ResponseEntity<Map<String, Object>> handleDownstreamServiceException(DownstreamServiceException ex) {
        Map<String, Object> body = Map.of(
                "status", ex.getStatus().value(),
                "error", ex.getStatus().getReasonPhrase(),
                "message", "Failed to process request due to an issue with a downstream service: " + ex.getMessage()
        );
        return new ResponseEntity<>(body, ex.getStatus());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        Map<String, Object> body = Map.of(
                "status", 500,
                "error", "Internal Server Error",
                "message", "An unexpected error occurred: " + ex.getMessage()
        );
        return new ResponseEntity<>(body, org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR);
    }
}