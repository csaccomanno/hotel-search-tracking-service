package com.riu.hotelsearch.application.service;

import com.riu.hotelsearch.application.exception.SearchNotFoundException;
import com.riu.hotelsearch.domain.model.HotelSearch;
import com.riu.hotelsearch.domain.model.SearchCriteria;
import com.riu.hotelsearch.domain.model.SearchCountResult;
import com.riu.hotelsearch.domain.port.out.SearchRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CountSearchesServiceTest {

    @Test
    void returnsSearchAndCount() {
        HotelSearch search = search();
        SearchCountResult expected = new SearchCountResult(search, 3);
        SearchRepository repository = mock(SearchRepository.class);
        when(repository.findCountById(search.searchId())).thenReturn(Optional.of(expected));
        CountSearchesService service = new CountSearchesService(repository);

        SearchCountResult result = service.countBySearchId(search.searchId());

        assertAll(
                () -> assertEquals(search, result.search()),
                () -> assertEquals(3, result.count()));
    }

    @Test
    void failsWhenSearchHasNotBeenPersisted() {
        UUID id = UUID.randomUUID();
        SearchRepository repository = mock(SearchRepository.class);
        when(repository.findCountById(id)).thenReturn(Optional.empty());
        CountSearchesService service = new CountSearchesService(repository);

        SearchNotFoundException exception = assertThrows(
                SearchNotFoundException.class, () -> service.countBySearchId(id));

        assertEquals("Search not found: " + id, exception.getMessage());
    }

    private HotelSearch search() {
        return new HotelSearch(UUID.randomUUID(), new SearchCriteria(
                "hotel", LocalDate.of(2026, 9, 10), LocalDate.of(2026, 9, 12), List.of(20)));
    }
}
