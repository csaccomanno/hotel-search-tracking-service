package com.riu.hotelsearch.infrastructure.adapter.in.kafka;

import com.riu.hotelsearch.application.port.in.PersistSearchUseCase;
import com.riu.hotelsearch.domain.model.HotelSearch;
import com.riu.hotelsearch.domain.model.SearchCriteria;
import com.riu.hotelsearch.infrastructure.adapter.out.kafka.SearchEventMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class KafkaSearchEventConsumer {

    private final PersistSearchUseCase persistSearchUseCase;

    @KafkaListener(topics = "${app.kafka.topic}", groupId = "${spring.kafka.consumer.group-id}")
    public void consume(
            SearchEventMessage message,
            @Header(KafkaHeaders.RECEIVED_KEY) String searchId) {
        log.info("Hotel search event received searchId={} virtualThread={}",
                searchId, Thread.currentThread().isVirtual());
        SearchCriteria criteria = new SearchCriteria(
                message.hotelId(), message.checkIn(), message.checkOut(), message.ages());
        persistSearchUseCase.persist(new HotelSearch(UUID.fromString(searchId), criteria));
        log.info("Hotel search persisted searchId={}", searchId);
    }
}
