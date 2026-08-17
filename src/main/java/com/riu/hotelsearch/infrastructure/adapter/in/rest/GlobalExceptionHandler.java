package com.riu.hotelsearch.infrastructure.adapter.in.rest;

import com.riu.hotelsearch.application.exception.SearchNotFoundException;
import com.riu.hotelsearch.domain.exception.DomainValidationException;
import com.riu.hotelsearch.infrastructure.adapter.in.rest.dto.ApiErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.List;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiErrorResponse> handleBeanValidation(MethodArgumentNotValidException exception) {
        List<String> messages = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .distinct()
                .toList();
        return response(HttpStatus.BAD_REQUEST, messages);
    }

    @ExceptionHandler({DomainValidationException.class, IllegalArgumentException.class})
    ResponseEntity<ApiErrorResponse> handleBadRequest(RuntimeException exception) {
        return response(HttpStatus.BAD_REQUEST, List.of(exception.getMessage()));
    }

    @ExceptionHandler(SearchNotFoundException.class)
    ResponseEntity<ApiErrorResponse> handleNotFound(SearchNotFoundException exception) {
        return response(HttpStatus.NOT_FOUND, List.of(exception.getMessage()));
    }

    private ResponseEntity<ApiErrorResponse> response(HttpStatus status, List<String> messages) {
        log.warn("Request rejected status={} errorType={}", status.value(), status.name());
        return ResponseEntity.status(status).body(new ApiErrorResponse(
                Instant.now(), status.value(), status.getReasonPhrase(), messages));
    }
}
