package com.riu.hotelsearch.application.service;

import com.riu.hotelsearch.application.port.in.RegisterSearchUseCase;
import com.riu.hotelsearch.application.port.out.SearchEventPublisher;
import com.riu.hotelsearch.domain.model.HotelSearch;
import com.riu.hotelsearch.domain.model.SearchCriteria;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.UUID;
import java.util.function.Supplier;

@RequiredArgsConstructor
public final class RegisterSearchService implements RegisterSearchUseCase {

    @NonNull
    private final SearchEventPublisher eventPublisher;
    @NonNull
    private final Supplier<UUID> idGenerator;

    @Override
    public UUID register(SearchCriteria criteria) {
        UUID searchId = idGenerator.get();
        HotelSearch search = new HotelSearch(searchId, criteria);
        eventPublisher.publish(search);
        return searchId;
    }
}
