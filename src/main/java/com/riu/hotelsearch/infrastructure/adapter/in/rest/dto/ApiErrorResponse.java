package com.riu.hotelsearch.infrastructure.adapter.in.rest.dto;

import java.time.Instant;
import java.util.List;

public record ApiErrorResponse(
        Instant timestamp,
        int status,
        String error,
        List<String> messages
) {
    public ApiErrorResponse {
        messages = List.copyOf(messages);
    }
}

