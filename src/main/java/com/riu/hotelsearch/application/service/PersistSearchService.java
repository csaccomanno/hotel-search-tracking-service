package com.riu.hotelsearch.application.service;

import com.riu.hotelsearch.application.port.in.PersistSearchUseCase;
import com.riu.hotelsearch.application.port.out.SearchRepository;
import com.riu.hotelsearch.domain.model.HotelSearch;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class PersistSearchService implements PersistSearchUseCase {

    @NonNull
    private final SearchRepository repository;

    @Override
    public void persist(HotelSearch search) {
        repository.saveIfAbsent(search);
    }
}
