package com.riu.hotelsearch.infrastructure.adapter.out.persistence;

import com.riu.hotelsearch.domain.model.HotelSearch;
import com.riu.hotelsearch.domain.model.SearchCountResult;
import com.riu.hotelsearch.domain.port.out.SearchRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class OracleSearchRepositoryAdapter implements SearchRepository {

    private final OracleSearchPackage searchPackage;

    @Override
    @Transactional
    public void saveAllIfAbsent(List<HotelSearch> searches) {
        searchPackage.persistAll(searches);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SearchCountResult> findCountById(UUID searchId) {
        return searchPackage.findCountById(searchId);
    }
}
