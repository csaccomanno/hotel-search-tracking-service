package com.riu.hotelsearch.application.service;

import com.riu.hotelsearch.application.exception.SearchNotFoundException;
import com.riu.hotelsearch.application.model.SearchCountResult;
import com.riu.hotelsearch.application.port.out.SearchRepository;
import com.riu.hotelsearch.domain.model.HotelSearch;
import com.riu.hotelsearch.domain.model.SearchCriteria;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CountSearchesServiceTest {

    @Test
    void returnsSearchAndCount() {
        HotelSearch search = search();
        SearchRepository repository = repository(search);
        CountSearchesService service = new CountSearchesService(repository);

        SearchCountResult result = service.countBySearchId(search.searchId());

        assertAll(
                () -> assertEquals(search, result.search()),
                () -> assertEquals(3, result.count()));
    }

    @Test
    void failsWhenSearchHasNotBeenPersisted() {
        UUID id = UUID.randomUUID();
        CountSearchesService service = new CountSearchesService(emptyRepository());

        SearchNotFoundException exception = assertThrows(
                SearchNotFoundException.class, () -> service.countBySearchId(id));

        assertEquals("Search not found: " + id, exception.getMessage());
    }

    private SearchRepository repository(HotelSearch result) {
        return new SearchRepository() {
            public void saveIfAbsent(HotelSearch search) {
            }

            public Optional<HotelSearch> findById(UUID searchId) {
                return Optional.of(result);
            }

            public long countMatching(SearchCriteria criteria) {
                return 3;
            }
        };
    }

    private SearchRepository emptyRepository() {
        return new SearchRepository() {
            public void saveIfAbsent(HotelSearch search) {
            }

            public Optional<HotelSearch> findById(UUID searchId) {
                return Optional.empty();
            }

            public long countMatching(SearchCriteria criteria) {
                return 0;
            }
        };
    }

    private HotelSearch search() {
        return new HotelSearch(UUID.randomUUID(), new SearchCriteria(
                "hotel", LocalDate.of(2026, 9, 10), LocalDate.of(2026, 9, 12), List.of(20)));
    }
}
