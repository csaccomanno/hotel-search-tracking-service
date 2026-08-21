package com.riu.hotelsearch.infrastructure.adapter.in.rest;

import com.riu.hotelsearch.domain.exception.DomainValidationException;
import com.riu.hotelsearch.domain.model.HotelSearch;
import com.riu.hotelsearch.domain.model.SearchCriteria;
import com.riu.hotelsearch.domain.model.SearchCountResult;
import com.riu.hotelsearch.infrastructure.adapter.in.rest.dto.SearchCountResponse;
import com.riu.hotelsearch.infrastructure.adapter.in.rest.dto.SearchRequest;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SearchRestMapperTest {

    @Test
    void mapsRequestAndResponsePreservingAgeOrder() {
        List<Integer> sourceAges = new ArrayList<>(List.of(30, 29, 1, 3));
        SearchRequest request = new SearchRequest("1234aBc", "29/12/2026", "31/12/2026", sourceAges);
        sourceAges.add(99);

        SearchCriteria criteria = SearchRestMapper.toDomain(request);
        UUID id = UUID.randomUUID();
        SearchCountResponse response = SearchRestMapper.toResponse(new SearchCountResult(
                new HotelSearch(id, criteria), 7));
        List<Integer> requestAges = request.ages();
        List<Integer> responseAges = response.search().ages();

        assertAll(
                () -> assertEquals(LocalDate.of(2026, 12, 29), criteria.checkIn()),
                () -> assertEquals(LocalDate.of(2026, 12, 31), criteria.checkOut()),
                () -> assertEquals(id, response.searchId()),
                () -> assertEquals("29/12/2026", response.search().checkIn()),
                () -> assertEquals(List.of(30, 29, 1, 3), requestAges),
                () -> assertThrows(UnsupportedOperationException.class, () -> requestAges.add(5)),
                () -> assertEquals(List.of(30, 29, 1, 3), responseAges),
                () -> assertThrows(UnsupportedOperationException.class, () -> responseAges.add(5)),
                () -> assertEquals(7, response.count()));
    }

    @Test
    void rejectsInvalidAndImpossibleDates() {
        SearchRequest invalidFormat = new SearchRequest(
                "hotel", "2026-12-29", "31/12/2026", List.of(1));
        SearchRequest impossibleDate = new SearchRequest(
                "hotel", "29/12/2026", "31/02/2027", List.of(1));

        assertAll(
                () -> assertThrows(DomainValidationException.class,
                        () -> SearchRestMapper.toDomain(invalidFormat)),
                () -> assertThrows(DomainValidationException.class,
                        () -> SearchRestMapper.toDomain(impossibleDate)));
    }
}
