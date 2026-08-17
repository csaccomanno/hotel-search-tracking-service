package com.riu.hotelsearch.infrastructure.adapter.out.kafka;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.util.List;

public record SearchEventMessage(
        String hotelId,
        @JsonFormat(pattern = "dd/MM/yyyy")
        LocalDate checkIn,
        @JsonFormat(pattern = "dd/MM/yyyy")
        LocalDate checkOut,
        List<Integer> ages
) {
    public SearchEventMessage {
        ages = List.copyOf(ages);
    }
}
