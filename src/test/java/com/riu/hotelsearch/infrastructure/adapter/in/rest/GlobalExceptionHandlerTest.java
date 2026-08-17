package com.riu.hotelsearch.infrastructure.adapter.in.rest;

import com.riu.hotelsearch.application.exception.SearchNotFoundException;
import com.riu.hotelsearch.domain.exception.DomainValidationException;
import com.riu.hotelsearch.infrastructure.adapter.in.rest.dto.ApiErrorResponse;
import com.riu.hotelsearch.infrastructure.adapter.in.rest.dto.SearchRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void mapsDomainAndNotFoundErrors() {
        ResponseEntity<ApiErrorResponse> bad = handler.handleBadRequest(
                new DomainValidationException("invalid dates"));
        ResponseEntity<ApiErrorResponse> missing = handler.handleNotFound(
                new SearchNotFoundException(UUID.fromString("e36b5a1e-0bce-4a35-b7ea-316928d51f09")));
        ApiErrorResponse badBody = Objects.requireNonNull(bad.getBody());

        assertAll(
                () -> assertEquals(400, bad.getStatusCode().value()),
                () -> assertEquals(List.of("invalid dates"), badBody.messages()),
                () -> assertThrows(UnsupportedOperationException.class,
                        () -> badBody.messages().add("another error")),
                () -> assertEquals(404, missing.getStatusCode().value()));
    }

    @Test
    void mapsBeanValidationErrors() throws Exception {
        SearchRequest target = new SearchRequest("", "", "", List.of());
        BeanPropertyBindingResult binding = new BeanPropertyBindingResult(target, "request");
        binding.rejectValue("hotelId", "blank", "hotelId must not be blank");
        binding.rejectValue("hotelId", "blank-again", "hotelId must not be blank");
        Method method = HotelSearchController.class.getMethod("register", SearchRequest.class);
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(
                new org.springframework.core.MethodParameter(method, 0), binding);

        ResponseEntity<ApiErrorResponse> response = handler.handleBeanValidation(exception);
        ApiErrorResponse responseBody = Objects.requireNonNull(response.getBody());

        assertAll(
                () -> assertEquals(400, response.getStatusCode().value()),
                () -> assertEquals(1, responseBody.messages().size()));
    }
}
