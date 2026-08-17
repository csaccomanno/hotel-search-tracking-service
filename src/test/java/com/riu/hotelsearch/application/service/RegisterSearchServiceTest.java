package com.riu.hotelsearch.application.service;

import com.riu.hotelsearch.application.port.out.SearchEventPublisher;
import com.riu.hotelsearch.domain.model.HotelSearch;
import com.riu.hotelsearch.domain.model.SearchCriteria;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class RegisterSearchServiceTest {

    @Test
    void generatesIdAndPublishesSearch() {
        UUID expectedId = UUID.randomUUID();
        CapturingPublisher publisher = new CapturingPublisher();
        RegisterSearchService service = new RegisterSearchService(publisher, () -> expectedId);
        SearchCriteria criteria = criteria();

        UUID result = service.register(criteria);

        assertAll(
                () -> assertEquals(expectedId, result),
                () -> assertEquals(expectedId, publisher.search.searchId()),
                () -> assertEquals(criteria, publisher.search.criteria()));
    }

    private SearchCriteria criteria() {
        return new SearchCriteria("hotel", LocalDate.of(2026, 9, 10),
                LocalDate.of(2026, 9, 12), List.of(30));
    }

    private static final class CapturingPublisher implements SearchEventPublisher {
        private HotelSearch search;

        @Override
        public void publish(HotelSearch search) {
            this.search = search;
        }
    }
}
