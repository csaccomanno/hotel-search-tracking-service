package com.riu.hotelsearch.domain.model;

import com.riu.hotelsearch.domain.exception.DomainValidationException;

import java.time.LocalDate;
import java.util.List;

public record SearchCriteria(
        String hotelId,
        LocalDate checkIn,
        LocalDate checkOut,
        List<Integer> ages
) {
    public SearchCriteria {
        if (hotelId == null || hotelId.isBlank()) {
            throw new DomainValidationException("hotelId must not be blank");
        }
        if (checkIn == null) {
            throw new DomainValidationException("checkIn must not be null");
        }
        if (checkOut == null) {
            throw new DomainValidationException("checkOut must not be null");
        }
        if (!checkIn.isBefore(checkOut)) {
            throw new DomainValidationException("checkIn must be before checkOut");
        }
        if (ages == null || ages.isEmpty()) {
            throw new DomainValidationException("ages must not be empty");
        }
        if (ages.stream().anyMatch(age -> age == null || age < 0)) {
            throw new DomainValidationException("ages must contain only non-negative values");
        }

        hotelId = hotelId.trim();
        ages = List.copyOf(ages);
    }
}

