package com.riu.hotelsearch.application.service;

import com.riu.hotelsearch.application.port.in.PersistSearchUseCase;
import com.riu.hotelsearch.domain.model.HotelSearch;
import com.riu.hotelsearch.domain.port.out.SearchRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public final class PersistSearchService implements PersistSearchUseCase {

    @NonNull
    private final SearchRepository repository;

    @Override
    public void persistAll(List<HotelSearch> searches) {
        if (!searches.isEmpty()) {
            repository.saveAllIfAbsent(List.copyOf(searches));
        }
    }
}
