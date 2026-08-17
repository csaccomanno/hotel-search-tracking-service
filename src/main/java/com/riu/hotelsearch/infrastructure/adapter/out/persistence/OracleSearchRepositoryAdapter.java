package com.riu.hotelsearch.infrastructure.adapter.out.persistence;

import com.riu.hotelsearch.application.port.out.SearchRepository;
import com.riu.hotelsearch.domain.model.HotelSearch;
import com.riu.hotelsearch.domain.model.SearchCriteria;
import com.riu.hotelsearch.infrastructure.adapter.out.persistence.entity.HotelSearchEntity;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Repository
@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class OracleSearchRepositoryAdapter implements SearchRepository {

    private final HotelSearchJpaRepository repository;

    @Override
    @Transactional
    public void saveIfAbsent(HotelSearch search) {
        String searchId = search.searchId().toString();
        if (repository.existsById(searchId)) {
            log.info("Hotel search already persisted searchId={}", searchId);
            return;
        }
        try {
            repository.saveAndFlush(toEntity(search));
            log.info("Hotel search stored searchId={}", searchId);
        } catch (DataIntegrityViolationException exception) {
            if (!repository.existsById(searchId)) {
                throw exception;
            }
            log.info("Concurrent duplicate hotel search ignored searchId={}", searchId);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<HotelSearch> findById(UUID searchId) {
        return repository.findById(searchId.toString()).map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public long countMatching(SearchCriteria criteria) {
        return repository.countMatching(
                criteria.hotelId(),
                criteria.checkIn(),
                criteria.checkOut(),
                AgesHash.calculate(criteria.ages()));
    }

    private HotelSearchEntity toEntity(HotelSearch search) {
        SearchCriteria criteria = search.criteria();
        return new HotelSearchEntity(
                search.searchId().toString(),
                criteria.hotelId(),
                criteria.checkIn(),
                criteria.checkOut(),
                AgesHash.calculate(criteria.ages()),
                criteria.ages());
    }

    private HotelSearch toDomain(HotelSearchEntity entity) {
        SearchCriteria criteria = new SearchCriteria(
                entity.getHotelId(),
                entity.getCheckIn(),
                entity.getCheckOut(),
                entity.getAges());
        return new HotelSearch(UUID.fromString(entity.getSearchId()), criteria);
    }
}
