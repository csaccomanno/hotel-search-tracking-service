package com.riu.hotelsearch.application.service;

import com.riu.hotelsearch.application.port.out.SearchEventPublisher;
import com.riu.hotelsearch.domain.exception.DomainValidationException;
import com.riu.hotelsearch.domain.model.HotelSearch;
import com.riu.hotelsearch.domain.model.SearchCriteria;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RegisterSearchServiceTest {

    private static final Clock CLOCK =
            Clock.fixed(Instant.parse("2026-08-20T00:00:00Z"), ZoneOffset.UTC);

    @Test
    void generatesIdAndPublishesSearch() {
        UUID expectedId = UUID.randomUUID();
        CapturingPublisher publisher = new CapturingPublisher();
        RegisterSearchService service = new RegisterSearchService(publisher, () -> expectedId, CLOCK);
        SearchCriteria criteria = criteria();

        UUID result = service.register(criteria);

        assertAll(
                () -> assertEquals(expectedId, result),
                () -> assertEquals(expectedId, publisher.search.searchId()),
                () -> assertEquals(criteria, publisher.search.criteria()));
    }

    @Test
    void rejectsPastCheckInBeforeGeneratingIdOrPublishing() {
        CapturingPublisher publisher = new CapturingPublisher();
        AtomicBoolean idGenerated = new AtomicBoolean();
        RegisterSearchService service = new RegisterSearchService(
                publisher,
                () -> {
                    idGenerated.set(true);
                    return UUID.randomUUID();
                },
                CLOCK);
        SearchCriteria criteria = new SearchCriteria(
                "hotel", LocalDate.of(2026, 8, 19),
                LocalDate.of(2026, 8, 21), List.of(30));

        var exception = assertThrows(
                DomainValidationException.class,
                () -> service.register(criteria));

        assertAll(
                () -> assertEquals("checkIn must not be in the past", exception.getMessage()),
                () -> assertFalse(idGenerated.get()),
                () -> assertNull(publisher.search));
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
