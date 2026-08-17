package com.riu.hotelsearch.application.port.out;

import com.riu.hotelsearch.domain.model.HotelSearch;
import com.riu.hotelsearch.domain.model.SearchCriteria;

import java.util.Optional;
import java.util.UUID;

public interface SearchRepository {
    void saveIfAbsent(HotelSearch search);

    Optional<HotelSearch> findById(UUID searchId);

    long countMatching(SearchCriteria criteria);
}

