package com.riu.hotelsearch.infrastructure.adapter.in.kafka;

import com.riu.hotelsearch.application.port.in.PersistSearchUseCase;
import com.riu.hotelsearch.domain.model.HotelSearch;
import com.riu.hotelsearch.infrastructure.adapter.out.kafka.SearchEventMessage;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

class KafkaSearchEventConsumerTest {

    @Test
    void mapsEventAndPersistsOnVirtualThread() throws Exception {
        CapturingUseCase useCase = new CapturingUseCase();
        KafkaSearchEventConsumer consumer = new KafkaSearchEventConsumer(useCase);
        UUID id = UUID.randomUUID();
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            executor.submit(() -> consumer.consume(new SearchEventMessage(
                    "hotel", LocalDate.of(2026, 9, 10),
                    LocalDate.of(2026, 9, 12), List.of(30, 1)), id.toString())).get();
        }

        assertAll(
                () -> assertEquals(id, useCase.search.searchId()),
                () -> assertEquals(List.of(30, 1), useCase.search.criteria().ages()),
                () -> assertTrue(useCase.virtualThread));
    }

    private static final class CapturingUseCase implements PersistSearchUseCase {
        private HotelSearch search;
        private boolean virtualThread;

        public void persist(HotelSearch search) {
            this.search = search;
            this.virtualThread = Thread.currentThread().isVirtual();
        }
    }
}
