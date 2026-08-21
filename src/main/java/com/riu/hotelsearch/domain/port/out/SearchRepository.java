package com.riu.hotelsearch.domain.port.out;

import com.riu.hotelsearch.domain.model.HotelSearch;
import com.riu.hotelsearch.domain.model.SearchCountResult;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SearchRepository {
    void saveAllIfAbsent(List<HotelSearch> searches);

    Optional<SearchCountResult> findCountById(UUID searchId);
}
