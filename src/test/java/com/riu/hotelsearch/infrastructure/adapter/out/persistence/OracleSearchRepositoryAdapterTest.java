package com.riu.hotelsearch.infrastructure.adapter.out.persistence;

import com.riu.hotelsearch.domain.model.HotelSearch;
import com.riu.hotelsearch.domain.model.SearchCriteria;
import com.riu.hotelsearch.domain.model.SearchCountResult;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OracleSearchRepositoryAdapterTest {

    @Test
    void delegatesSearchBatchToProcedure() {
        OracleSearchPackage oraclePackage = mock(OracleSearchPackage.class);
        OracleSearchRepositoryAdapter adapter = new OracleSearchRepositoryAdapter(oraclePackage);
        HotelSearch search = search();
        adapter.saveAllIfAbsent(List.of(search));

        verify(oraclePackage).persistAll(List.of(search));
    }

    @Test
    void findsAndCountsSearches() {
        HotelSearch search = search();
        SearchCountResult expected = new SearchCountResult(search, 5);
        OracleSearchPackage oraclePackage = mock(OracleSearchPackage.class);
        when(oraclePackage.findCountById(search.searchId())).thenReturn(Optional.of(expected));
        OracleSearchRepositoryAdapter adapter = new OracleSearchRepositoryAdapter(oraclePackage);

        Optional<SearchCountResult> result = adapter.findCountById(search.searchId());

        assertAll(
                () -> assertEquals(Optional.of(search), result.map(SearchCountResult::search)),
                () -> assertEquals(5, result.orElseThrow().count()),
                () -> verify(oraclePackage).findCountById(search.searchId()),
                () -> verifyNoMoreInteractions(oraclePackage));
    }

    private HotelSearch search() {
        return new HotelSearch(
                UUID.fromString("e36b5a1e-0bce-4a35-b7ea-316928d51f09"),
                new SearchCriteria("hotel", LocalDate.of(2026, 9, 10),
                        LocalDate.of(2026, 9, 12), List.of(30, 1)));
    }
}
