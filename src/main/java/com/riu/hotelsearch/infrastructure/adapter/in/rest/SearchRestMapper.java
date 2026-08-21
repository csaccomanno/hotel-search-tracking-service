package com.riu.hotelsearch.infrastructure.adapter.in.rest;

import com.riu.hotelsearch.domain.exception.DomainValidationException;
import com.riu.hotelsearch.domain.model.SearchCountResult;
import com.riu.hotelsearch.domain.model.SearchCriteria;
import com.riu.hotelsearch.infrastructure.adapter.in.rest.dto.SearchCountResponse;
import com.riu.hotelsearch.infrastructure.adapter.in.rest.dto.SearchDetailsResponse;
import com.riu.hotelsearch.infrastructure.adapter.in.rest.dto.SearchRequest;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

final class SearchRestMapper {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter
            .ofPattern("dd/MM/uuuu")
            .withResolverStyle(ResolverStyle.STRICT);

    private SearchRestMapper() {
    }

    static SearchCriteria toDomain(SearchRequest request) {
        LocalDate checkIn = parseDate(request.checkIn(), "checkIn");
        return new SearchCriteria(
                request.hotelId(),
                checkIn,
                parseDate(request.checkOut(), "checkOut"),
                request.ages());
    }

    static SearchCountResponse toResponse(SearchCountResult result) {
        SearchCriteria criteria = result.search().criteria();
        return new SearchCountResponse(
                result.search().searchId(),
                new SearchDetailsResponse(
                        criteria.hotelId(),
                        DATE_FORMAT.format(criteria.checkIn()),
                        DATE_FORMAT.format(criteria.checkOut()),
                        criteria.ages()),
                result.count());
    }

    private static LocalDate parseDate(String value, String field) {
        try {
            return LocalDate.parse(value, DATE_FORMAT);
        } catch (DateTimeParseException exception) {
            throw new DomainValidationException(field + " must use dd/MM/yyyy format");
        }
    }
}
