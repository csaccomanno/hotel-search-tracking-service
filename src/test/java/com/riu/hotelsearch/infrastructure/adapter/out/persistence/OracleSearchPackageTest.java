package com.riu.hotelsearch.infrastructure.adapter.out.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.riu.hotelsearch.domain.model.HotelSearch;
import com.riu.hotelsearch.domain.model.SearchCountResult;
import com.riu.hotelsearch.domain.model.SearchCriteria;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.jdbc.core.CallableStatementCallback;
import org.springframework.jdbc.core.CallableStatementCreator;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OracleSearchPackageTest {

    private static final UUID SEARCH_ID =
            UUID.fromString("e36b5a1e-0bce-4a35-b7ea-316928d51f09");

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void serializesAndPersistsTheWholeBatchInOneCall() throws Exception {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        OracleSearchPackage oraclePackage = new OracleSearchPackage(jdbcTemplate);
        HotelSearch first = search(SEARCH_ID.toString(), "hotel-1", List.of(30, 1));
        HotelSearch second = search(
                "4a5554c7-3d18-4c5f-87b2-6f254c5b285b", "hotel-2", List.of(40));
        ArgumentCaptor<String> payload = ArgumentCaptor.forClass(String.class);

        oraclePackage.persistAll(List.of(first, second));

        verify(jdbcTemplate).update(
                eq("{call hotel_search_pkg.persist_search_batch(?)}"), payload.capture());
        var json = objectMapper.readTree(payload.getValue());
        assertAll(
                () -> assertEquals(2, json.size()),
                () -> assertEquals(first.searchId().toString(), json.get(0).get("searchId").asText()),
                () -> assertEquals("hotel-1", json.get(0).get("hotelId").asText()),
                () -> assertEquals("2026-09-10", json.get(0).get("checkIn").asText()),
                () -> assertEquals(AgesHash.calculate(List.of(30, 1)),
                        json.get(0).get("agesHash").asText()),
                () -> assertEquals(2, json.get(0).get("ages").size()),
                () -> assertEquals(second.searchId().toString(),
                        json.get(1).get("searchId").asText()));
        verifyNoMoreInteractions(jdbcTemplate);
    }

    @Test
    void mapsTheSearchCountCursor() throws Exception {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        Connection connection = mock(Connection.class);
        CallableStatement statement = mock(CallableStatement.class);
        ResultSet resultSet = mock(ResultSet.class);
        when(connection.prepareCall("{call hotel_search_pkg.find_search_count(?, ?)}"))
                .thenReturn(statement);
        when(statement.getObject(2)).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString(1)).thenReturn(SEARCH_ID.toString());
        when(resultSet.getString(2)).thenReturn("hotel");
        when(resultSet.getTimestamp(3)).thenReturn(Timestamp.valueOf("2026-09-10 00:00:00"));
        when(resultSet.getTimestamp(4)).thenReturn(Timestamp.valueOf("2026-09-12 00:00:00"));
        when(resultSet.getString(5)).thenReturn("[30,1]");
        when(resultSet.getLong(6)).thenReturn(5L);
        executeCallbacks(jdbcTemplate, connection, statement);
        OracleSearchPackage oraclePackage = new OracleSearchPackage(jdbcTemplate);

        Optional<SearchCountResult> result = oraclePackage.findCountById(SEARCH_ID);

        SearchCountResult count = result.orElseThrow();
        assertAll(
                () -> assertEquals(SEARCH_ID, count.search().searchId()),
                () -> assertEquals("hotel", count.search().criteria().hotelId()),
                () -> assertEquals(List.of(30, 1), count.search().criteria().ages()),
                () -> assertEquals(5, count.count()),
                () -> verify(statement).setString(1, SEARCH_ID.toString()),
                () -> verify(statement).registerOutParameter(2, Types.REF_CURSOR));
    }

    @Test
    void returnsEmptyWhenTheCursorHasNoRows() throws Exception {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        Connection connection = mock(Connection.class);
        CallableStatement statement = mock(CallableStatement.class);
        ResultSet resultSet = mock(ResultSet.class);
        when(connection.prepareCall("{call hotel_search_pkg.find_search_count(?, ?)}"))
                .thenReturn(statement);
        when(statement.getObject(2)).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);
        executeCallbacks(jdbcTemplate, connection, statement);
        OracleSearchPackage oraclePackage = new OracleSearchPackage(jdbcTemplate);

        Optional<SearchCountResult> result = oraclePackage.findCountById(SEARCH_ID);

        assertTrue(result.isEmpty());
    }

    @SuppressWarnings("unchecked")
    private void executeCallbacks(
            JdbcTemplate jdbcTemplate,
            Connection connection,
            CallableStatement statement) {
        when(jdbcTemplate.execute(
                any(CallableStatementCreator.class),
                any(CallableStatementCallback.class)))
                .thenAnswer(invocation -> {
                    CallableStatementCreator creator = invocation.getArgument(0);
                    CallableStatementCallback<Optional<SearchCountResult>> callback =
                            invocation.getArgument(1);
                    creator.createCallableStatement(connection);
                    return callback.doInCallableStatement(statement);
                });
    }

    private HotelSearch search(String id, String hotelId, List<Integer> ages) {
        return new HotelSearch(
                UUID.fromString(id),
                new SearchCriteria(hotelId, LocalDate.of(2026, 9, 10),
                        LocalDate.of(2026, 9, 12), ages));
    }
}
