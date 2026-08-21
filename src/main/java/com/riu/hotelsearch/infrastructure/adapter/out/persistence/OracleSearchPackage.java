package com.riu.hotelsearch.infrastructure.adapter.out.persistence;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.riu.hotelsearch.domain.model.HotelSearch;
import com.riu.hotelsearch.domain.model.SearchCountResult;
import com.riu.hotelsearch.domain.model.SearchCriteria;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.CallableStatementCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class OracleSearchPackage {

    private static final String PERSIST_BATCH_CALL =
            "{call hotel_search_pkg.persist_search_batch(?)}";
    private static final String FIND_COUNT_CALL =
            "{call hotel_search_pkg.find_search_count(?, ?)}";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final TypeReference<List<Integer>> AGES_TYPE = new TypeReference<>() {
    };

    private final JdbcTemplate jdbcTemplate;

    void persistAll(List<HotelSearch> searches) {
        jdbcTemplate.update(PERSIST_BATCH_CALL, serialize(searches));
    }

    Optional<SearchCountResult> findCountById(UUID searchId) {
        Optional<SearchCountResult> result = jdbcTemplate.execute(
                connection -> {
                    var statement = connection.prepareCall(FIND_COUNT_CALL);
                    statement.setString(1, searchId.toString());
                    statement.registerOutParameter(2, Types.REF_CURSOR);
                    return statement;
                },
                (CallableStatementCallback<Optional<SearchCountResult>>) statement -> {
                    statement.execute();
                    try (ResultSet resultSet = (ResultSet) statement.getObject(2)) {
                        return resultSet.next()
                                ? Optional.of(map(resultSet))
                                : Optional.empty();
                    }
                });
        return Objects.requireNonNull(result);
    }

    private SearchCountResult map(ResultSet resultSet) throws SQLException {
        SearchCriteria criteria = new SearchCriteria(
                resultSet.getString(2),
                resultSet.getTimestamp(3).toLocalDateTime().toLocalDate(),
                resultSet.getTimestamp(4).toLocalDateTime().toLocalDate(),
                deserializeAges(resultSet.getString(5)));
        HotelSearch search = new HotelSearch(UUID.fromString(resultSet.getString(1)), criteria);
        return new SearchCountResult(search, resultSet.getLong(6));
    }

    private String serialize(List<HotelSearch> searches) {
        List<SearchBatchEntry> entries = searches.stream()
                .map(SearchBatchEntry::from)
                .toList();
        try {
            return OBJECT_MAPPER.writeValueAsString(entries);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Unable to serialize hotel search batch", exception);
        }
    }

    private List<Integer> deserializeAges(String agesJson) {
        try {
            return OBJECT_MAPPER.readValue(agesJson, AGES_TYPE);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Unable to deserialize hotel search ages", exception);
        }
    }

    private record SearchBatchEntry(
            String searchId,
            String hotelId,
            String checkIn,
            String checkOut,
            String agesHash,
            List<Integer> ages
    ) {
        private static SearchBatchEntry from(HotelSearch search) {
            return new SearchBatchEntry(
                    search.searchId().toString(),
                    search.criteria().hotelId(),
                    search.criteria().checkIn().toString(),
                    search.criteria().checkOut().toString(),
                    AgesHash.calculate(search.criteria().ages()),
                    search.criteria().ages());
        }
    }
}
