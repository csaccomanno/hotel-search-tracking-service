package com.riu.hotelsearch.domain.model;

import com.riu.hotelsearch.domain.exception.DomainValidationException;

import java.util.Objects;
import java.util.UUID;

public record HotelSearch(UUID searchId, SearchCriteria criteria) {

    public HotelSearch {
        if (searchId == null) {
            throw new DomainValidationException("searchId must not be null");
        }
        Objects.requireNonNull(criteria, "criteria must not be null");
    }
}
