package com.chatpress.v1.artifact;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(DuplicateSlugException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicateSlug(DuplicateSlugException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiErrorResponse("DUPLICATE_SLUG", exception.getMessage()));
    }
}
