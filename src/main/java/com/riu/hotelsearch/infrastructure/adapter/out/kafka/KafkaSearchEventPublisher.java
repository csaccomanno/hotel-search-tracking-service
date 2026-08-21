package com.riu.hotelsearch.infrastructure.adapter.out.kafka;

import com.riu.hotelsearch.application.port.out.SearchEventPublisher;
import com.riu.hotelsearch.domain.model.HotelSearch;
import com.riu.hotelsearch.infrastructure.messaging.kafka.SearchEventMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaSearchEventPublisher implements SearchEventPublisher {

    private final KafkaTemplate<String, SearchEventMessage> kafkaTemplate;
    private final String topic;

    public KafkaSearchEventPublisher(
            KafkaTemplate<String, SearchEventMessage> kafkaTemplate,
            @Value("${app.kafka.topic}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    @Override
    public void publish(HotelSearch search) {
        SearchEventMessage message = new SearchEventMessage(
                search.criteria().hotelId(),
                search.criteria().checkIn(),
                search.criteria().checkOut(),
                search.criteria().ages());
        kafkaTemplate.send(topic, search.searchId().toString(), message).join();
    }
}
