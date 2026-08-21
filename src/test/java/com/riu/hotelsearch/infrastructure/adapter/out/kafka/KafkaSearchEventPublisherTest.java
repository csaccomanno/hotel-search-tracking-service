package com.riu.hotelsearch.infrastructure.adapter.out.kafka;

import com.riu.hotelsearch.domain.model.HotelSearch;
import com.riu.hotelsearch.domain.model.SearchCriteria;
import com.riu.hotelsearch.infrastructure.messaging.kafka.SearchEventMessage;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class KafkaSearchEventPublisherTest {

    @Test
    void createsImmutableMessage() {
        List<Integer> sourceAges = new ArrayList<>(List.of(30, 1));
        SearchEventMessage message = new SearchEventMessage(
                "hotel", LocalDate.of(2026, 9, 10), LocalDate.of(2026, 9, 12), sourceAges);
        sourceAges.add(99);
        List<Integer> immutableAges = message.ages();

        assertAll(
                () -> assertEquals(List.of(30, 1), immutableAges),
                () -> assertThrows(UnsupportedOperationException.class, () -> immutableAges.add(5)));
    }

    @Test
    @SuppressWarnings("unchecked")
    void publishesSearchPayloadUsingSearchIdAsKey() {
        KafkaTemplate<String, SearchEventMessage> template = mock(KafkaTemplate.class);
        when(template.send(eq("topic"), any(String.class), any(SearchEventMessage.class)))
                .thenReturn(CompletableFuture.completedFuture(mock(SendResult.class)));
        KafkaSearchEventPublisher publisher = new KafkaSearchEventPublisher(template, "topic");
        UUID id = UUID.randomUUID();
        HotelSearch search = new HotelSearch(id, new SearchCriteria(
                "hotel", LocalDate.of(2026, 9, 10), LocalDate.of(2026, 9, 12), List.of(30, 1)));

        publisher.publish(search);

        var captor = org.mockito.ArgumentCaptor.forClass(SearchEventMessage.class);
        verify(template).send(eq("topic"), eq(id.toString()), captor.capture());
        SearchEventMessage message = captor.getValue();
        String json;
        try (JacksonJsonSerializer<SearchEventMessage> serializer = new JacksonJsonSerializer<>()) {
            json = new String(Objects.requireNonNull(serializer.serialize("topic", message)), StandardCharsets.UTF_8);
        }
        assertAll(
                () -> assertEquals("hotel", message.hotelId()),
                () -> assertEquals(List.of(30, 1), message.ages()),
                () -> assertEquals(
                        "{\"hotelId\":\"hotel\",\"checkIn\":\"10/09/2026\",\"checkOut\":\"12/09/2026\",\"ages\":[30,1]}",
                        json));
    }
}
