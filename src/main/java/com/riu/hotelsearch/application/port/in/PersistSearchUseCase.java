package com.riu.hotelsearch.application.port.in;

import com.riu.hotelsearch.domain.model.HotelSearch;

public interface PersistSearchUseCase {
    void persist(HotelSearch search);
}

