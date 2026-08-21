package com.riu.hotelsearch.infrastructure.adapter.in.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.util.List;

public record SearchRequest(
        @NotBlank(message = "hotelId must not be blank")
        @Size(max = 100, message = "hotelId must contain at most 100 characters") String hotelId,
        @NotBlank(message = "checkIn must not be blank") String checkIn,
        @NotBlank(message = "checkOut must not be blank") String checkOut,
        @NotNull(message = "ages must not be null")
        @Size(min = 1, message = "ages must not be empty")
        List<@NotNull(message = "age must not be null")
        @PositiveOrZero(message = "age must be greater than or equal to zero") Integer> ages
) {
    public SearchRequest {
        if (ages != null) {
            ages = List.copyOf(ages);
        }
    }
}
