package com.riu.hotelsearch.application.service;

import com.riu.hotelsearch.domain.model.HotelSearch;
import com.riu.hotelsearch.domain.model.SearchCriteria;
import com.riu.hotelsearch.domain.model.SearchCountResult;
import com.riu.hotelsearch.domain.port.out.SearchRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PersistSearchServiceTest {

    @Test
    void delegatesPersistence() {
        CapturingRepository repository = new CapturingRepository();
        PersistSearchService service = new PersistSearchService(repository);
        HotelSearch search = search();

        service.persistAll(List.of(search));

        assertEquals(List.of(search), repository.saved);
    }

    private HotelSearch search() {
        return new HotelSearch(UUID.randomUUID(), new SearchCriteria(
                "hotel", LocalDate.of(2026, 9, 10), LocalDate.of(2026, 9, 12), List.of(20)));
    }

    private static final class CapturingRepository implements SearchRepository {
        private List<HotelSearch> saved;

        public void saveAllIfAbsent(List<HotelSearch> searches) {
            saved = searches;
        }

        public Optional<SearchCountResult> findCountById(UUID searchId) {
            return Optional.empty();
        }
    }
}
