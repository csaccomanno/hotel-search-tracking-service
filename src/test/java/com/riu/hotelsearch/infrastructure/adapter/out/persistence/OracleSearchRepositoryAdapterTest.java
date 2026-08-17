package com.riu.hotelsearch.infrastructure.adapter.out.persistence;

import com.riu.hotelsearch.domain.model.HotelSearch;
import com.riu.hotelsearch.domain.model.SearchCriteria;
import com.riu.hotelsearch.infrastructure.adapter.out.persistence.entity.HotelSearchEntity;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class OracleSearchRepositoryAdapterTest {

    @Test
    void savesAndMapsSearch() {
        HotelSearchJpaRepository jpa = mock(HotelSearchJpaRepository.class);
        OracleSearchRepositoryAdapter adapter = new OracleSearchRepositoryAdapter(jpa);
        HotelSearch search = search();
        when(jpa.existsById(search.searchId().toString())).thenReturn(false);

        adapter.saveIfAbsent(search);

        var captor = org.mockito.ArgumentCaptor.forClass(HotelSearchEntity.class);
        verify(jpa).saveAndFlush(captor.capture());
        assertAll(
                () -> assertEquals(search.searchId().toString(), captor.getValue().getSearchId()),
                () -> assertEquals(search.criteria().ages(), captor.getValue().getAges()));
    }

    @Test
    void ignoresAlreadyPersistedSearch() {
        HotelSearchJpaRepository jpa = mock(HotelSearchJpaRepository.class);
        HotelSearch search = search();
        when(jpa.existsById(search.searchId().toString())).thenReturn(true);

        new OracleSearchRepositoryAdapter(jpa).saveIfAbsent(search);

        verify(jpa, never()).saveAndFlush(any());
    }

    @Test
    void toleratesConcurrentDuplicateButDoesNotHideOtherConstraintFailures() {
        HotelSearch search = search();
        String id = search.searchId().toString();
        HotelSearchJpaRepository duplicateJpa = mock(HotelSearchJpaRepository.class);
        when(duplicateJpa.existsById(id)).thenReturn(false, true);
        when(duplicateJpa.saveAndFlush(any())).thenThrow(new DataIntegrityViolationException("duplicate"));

        new OracleSearchRepositoryAdapter(duplicateJpa).saveIfAbsent(search);

        HotelSearchJpaRepository invalidJpa = mock(HotelSearchJpaRepository.class);
        when(invalidJpa.existsById(id)).thenReturn(false);
        when(invalidJpa.saveAndFlush(any())).thenThrow(new DataIntegrityViolationException("other"));
        OracleSearchRepositoryAdapter invalidAdapter = new OracleSearchRepositoryAdapter(invalidJpa);
        assertThrows(DataIntegrityViolationException.class, () -> invalidAdapter.saveIfAbsent(search));
    }

    @Test
    void findsAndCountsSearches() {
        HotelSearch search = search();
        HotelSearchEntity entity = new HotelSearchEntity(
                search.searchId().toString(), search.criteria().hotelId(), search.criteria().checkIn(),
                search.criteria().checkOut(), AgesHash.calculate(search.criteria().ages()),
                search.criteria().ages());
        HotelSearchJpaRepository jpa = mock(HotelSearchJpaRepository.class);
        when(jpa.findById(search.searchId().toString())).thenReturn(Optional.of(entity));
        when(jpa.countMatching(
                search.criteria().hotelId(), search.criteria().checkIn(), search.criteria().checkOut(),
                AgesHash.calculate(search.criteria().ages()))).thenReturn(5L);
        OracleSearchRepositoryAdapter adapter = new OracleSearchRepositoryAdapter(jpa);

        Optional<HotelSearch> found = adapter.findById(search.searchId());
        long count = adapter.countMatching(search.criteria());

        assertAll(
                () -> assertEquals(Optional.of(search), found),
                () -> assertEquals(5, count));
    }

    private HotelSearch search() {
        return new HotelSearch(
                UUID.fromString("e36b5a1e-0bce-4a35-b7ea-316928d51f09"),
                new SearchCriteria("hotel", LocalDate.of(2026, 9, 10),
                        LocalDate.of(2026, 9, 12), List.of(30, 1)));
    }
}
