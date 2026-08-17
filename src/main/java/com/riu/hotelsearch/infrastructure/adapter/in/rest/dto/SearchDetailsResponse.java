package com.riu.hotelsearch.infrastructure.adapter.in.rest.dto;

import java.util.List;

public record SearchDetailsResponse(
        String hotelId,
        String checkIn,
        String checkOut,
        List<Integer> ages
) {
    public SearchDetailsResponse {
        ages = List.copyOf(ages);
    }
}
