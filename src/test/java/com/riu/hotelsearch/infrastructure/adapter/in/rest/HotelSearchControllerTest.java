package com.riu.hotelsearch.infrastructure.adapter.in.rest;

import com.riu.hotelsearch.application.exception.SearchNotFoundException;
import com.riu.hotelsearch.application.port.in.CountSearchesUseCase;
import com.riu.hotelsearch.application.port.in.RegisterSearchUseCase;
import com.riu.hotelsearch.domain.model.HotelSearch;
import com.riu.hotelsearch.domain.model.SearchCriteria;
import com.riu.hotelsearch.domain.model.SearchCountResult;
import com.riu.hotelsearch.infrastructure.adapter.in.rest.dto.SearchCountResponse;
import com.riu.hotelsearch.infrastructure.adapter.in.rest.dto.SearchIdResponse;
import com.riu.hotelsearch.infrastructure.adapter.in.rest.dto.SearchRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class HotelSearchControllerTest {

    @Test
    void delegatesBothEndpoints() throws NoSuchMethodException {
        UUID id = UUID.randomUUID();
        SearchCriteria criteria = new SearchCriteria(
                "1234aBc", LocalDate.of(2026, 12, 29), LocalDate.of(2026, 12, 31), List.of(30, 29));
        RegisterSearchUseCase register = ignored -> id;
        CountSearchesUseCase count = ignored -> new SearchCountResult(
                new HotelSearch(id, criteria), 4);
        HotelSearchController controller = new HotelSearchController(register, count);

        SearchIdResponse registered = controller.register(new SearchRequest(
                "1234aBc", "29/12/2026", "31/12/2026", List.of(30, 29)));
        SearchCountResponse counted = controller.count(id.toString());
        ResponseStatus status = HotelSearchController.class
                .getMethod("register", SearchRequest.class)
                .getAnnotation(ResponseStatus.class);

        assertAll(
                () -> assertEquals(id, registered.searchId()),
                () -> assertEquals(id, counted.searchId()),
                () -> assertEquals(4, counted.count()),
                () -> assertEquals(HttpStatus.CREATED, status.value()),
                () -> assertThrows(SearchNotFoundException.class,
                        () -> controller.count("nonexistent-id-xyz-99999")));
    }
}
