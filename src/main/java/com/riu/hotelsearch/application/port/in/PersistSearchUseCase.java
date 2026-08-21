package com.riu.hotelsearch.application.port.in;

import com.riu.hotelsearch.domain.model.HotelSearch;

import java.util.List;

public interface PersistSearchUseCase {
    void persistAll(List<HotelSearch> searches);
}
