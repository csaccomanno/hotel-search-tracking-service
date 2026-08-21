package com.riu.hotelsearch.infrastructure.adapter.in.kafka;

import com.riu.hotelsearch.application.port.in.PersistSearchUseCase;
import com.riu.hotelsearch.domain.model.HotelSearch;
import com.riu.hotelsearch.infrastructure.messaging.kafka.SearchEventMessage;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class KafkaSearchEventConsumerTest {

    @Test
    void mapsAndPersistsARecordBatch() {
        CapturingUseCase useCase = new CapturingUseCase();
        KafkaSearchEventConsumer consumer = new KafkaSearchEventConsumer(useCase);
        UUID firstId = UUID.randomUUID();
        UUID secondId = UUID.randomUUID();

        consumer.consume(List.of(
                record(firstId, "hotel-1", List.of(30, 1)),
                record(secondId, "hotel-2", List.of(40))));

        assertAll(
                () -> assertEquals(List.of(firstId, secondId),
                        useCase.searches.stream().map(HotelSearch::searchId).toList()),
                () -> assertEquals(List.of(30, 1), useCase.searches.getFirst().criteria().ages()));
    }

    private ConsumerRecord<String, SearchEventMessage> record(UUID id, String hotelId, List<Integer> ages) {
        return new ConsumerRecord<>("topic", 0, 0L, id.toString(), new SearchEventMessage(
                hotelId, LocalDate.of(2026, 9, 10), LocalDate.of(2026, 9, 12), ages));
    }

    private static final class CapturingUseCase implements PersistSearchUseCase {
        private List<HotelSearch> searches;

        public void persistAll(List<HotelSearch> searches) {
            this.searches = searches;
        }
    }
}
