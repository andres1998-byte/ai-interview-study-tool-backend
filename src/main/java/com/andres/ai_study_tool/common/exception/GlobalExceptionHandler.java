package com.andres.ai_study_tool.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidInterviewRequestException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidInterview(
            InvalidInterviewRequestException e
    ) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                        "error", "INVALID_INTERVIEW_REQUEST",
                        "message", safeMessage(e.getMessage())
                ));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalState(
            IllegalStateException e
    ) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                        "error", "INVALID_FLOW_STATE",
                        "message", safeMessage(e.getMessage())
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception e) {
        // TODO: replace with proper logger later
        e.printStackTrace();

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                        "error", "INTERNAL_SERVER_ERROR",
                        "message", "Something went wrong on our side. Please try again."
                ));
    }

    private String safeMessage(String message) {
        if (message == null || message.isBlank()) {
            return "An unexpected error occurred.";
        }
        return message;
    }
}
