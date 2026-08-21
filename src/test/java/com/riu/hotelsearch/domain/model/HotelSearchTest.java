package com.riu.hotelsearch.domain.model;

import com.riu.hotelsearch.domain.exception.DomainValidationException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class HotelSearchTest {

    private final SearchCriteria criteria = new SearchCriteria(
            "hotel", LocalDate.of(2026, 9, 10), LocalDate.of(2026, 9, 12), List.of(25));

    @Test
    void createsHotelSearch() {
        UUID id = UUID.randomUUID();
        HotelSearch search = new HotelSearch(id, criteria);

        assertAll(
                () -> assertEquals(id, search.searchId()),
                () -> assertEquals(criteria, search.criteria()));
    }

    @Test
    void rejectsNullValues() {
        UUID searchId = UUID.randomUUID();

        assertAll(
                () -> assertThrows(DomainValidationException.class,
                        () -> new HotelSearch(null, criteria)),
                () -> assertThrows(NullPointerException.class,
                        () -> new HotelSearch(searchId, null)));
    }
}
