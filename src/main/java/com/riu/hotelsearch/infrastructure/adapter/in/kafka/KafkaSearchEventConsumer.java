package com.riu.hotelsearch.infrastructure.adapter.in.kafka;

import com.riu.hotelsearch.application.port.in.PersistSearchUseCase;
import com.riu.hotelsearch.domain.model.HotelSearch;
import com.riu.hotelsearch.domain.model.SearchCriteria;
import com.riu.hotelsearch.infrastructure.messaging.kafka.SearchEventMessage;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class KafkaSearchEventConsumer {

    private final PersistSearchUseCase persistSearchUseCase;

    @KafkaListener(topics = "${app.kafka.topic}")
    public void consume(List<ConsumerRecord<String, SearchEventMessage>> records) {
        List<HotelSearch> searches = records.stream()
                .map(this::toDomain)
                .toList();
        persistSearchUseCase.persistAll(searches);
    }

    private HotelSearch toDomain(ConsumerRecord<String, SearchEventMessage> record) {
        SearchEventMessage message = record.value();
        SearchCriteria criteria = new SearchCriteria(
                message.hotelId(), message.checkIn(), message.checkOut(), message.ages());
        return new HotelSearch(UUID.fromString(record.key()), criteria);
    }
}
