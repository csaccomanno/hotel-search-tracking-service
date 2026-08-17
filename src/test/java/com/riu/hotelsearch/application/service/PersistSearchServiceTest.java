package com.riu.hotelsearch.application.service;

import com.riu.hotelsearch.application.port.out.SearchRepository;
import com.riu.hotelsearch.domain.model.HotelSearch;
import com.riu.hotelsearch.domain.model.SearchCriteria;
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

        service.persist(search);

        assertEquals(search, repository.saved);
    }

    private HotelSearch search() {
        return new HotelSearch(UUID.randomUUID(), new SearchCriteria(
                "hotel", LocalDate.of(2026, 9, 10), LocalDate.of(2026, 9, 12), List.of(20)));
    }

    private static final class CapturingRepository implements SearchRepository {
        private HotelSearch saved;

        public void saveIfAbsent(HotelSearch search) {
            saved = search;
        }

        public Optional<HotelSearch> findById(UUID searchId) {
            return Optional.empty();
        }

        public long countMatching(SearchCriteria criteria) {
            return 0;
        }
    }
}
