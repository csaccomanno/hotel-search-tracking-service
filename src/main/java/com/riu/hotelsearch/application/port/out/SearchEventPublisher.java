package com.riu.hotelsearch.application.port.out;

import com.riu.hotelsearch.domain.model.HotelSearch;

public interface SearchEventPublisher {
    void publish(HotelSearch search);
}

