package com.riu.hotelsearch.application.service;

import com.riu.hotelsearch.application.exception.SearchNotFoundException;
import com.riu.hotelsearch.application.model.SearchCountResult;
import com.riu.hotelsearch.application.port.in.CountSearchesUseCase;
import com.riu.hotelsearch.application.port.out.SearchRepository;
import com.riu.hotelsearch.domain.model.HotelSearch;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public final class CountSearchesService implements CountSearchesUseCase {

    @NonNull
    private final SearchRepository repository;

    @Override
    public SearchCountResult countBySearchId(UUID searchId) {
        HotelSearch search = repository.findById(searchId)
                .orElseThrow(() -> new SearchNotFoundException(searchId));
        return new SearchCountResult(search, repository.countMatching(search.criteria()));
    }
}
