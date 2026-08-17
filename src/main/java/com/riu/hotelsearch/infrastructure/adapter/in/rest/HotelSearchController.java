package com.riu.hotelsearch.infrastructure.adapter.in.rest;

import com.riu.hotelsearch.application.port.in.CountSearchesUseCase;
import com.riu.hotelsearch.application.port.in.RegisterSearchUseCase;
import com.riu.hotelsearch.infrastructure.adapter.in.rest.dto.SearchCountResponse;
import com.riu.hotelsearch.infrastructure.adapter.in.rest.dto.SearchIdResponse;
import com.riu.hotelsearch.infrastructure.adapter.in.rest.dto.SearchRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping
@Slf4j
@RequiredArgsConstructor
public class HotelSearchController {

    private final RegisterSearchUseCase registerSearchUseCase;
    private final CountSearchesUseCase countSearchesUseCase;

    @PostMapping("/search")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public SearchIdResponse register(@Valid @RequestBody SearchRequest request) {
        UUID searchId = registerSearchUseCase.register(SearchRestMapper.toDomain(request));
        log.info("Hotel search accepted searchId={} hotelId={}", searchId, request.hotelId());
        return new SearchIdResponse(searchId);
    }

    @GetMapping("/count")
    public SearchCountResponse count(@RequestParam UUID searchId) {
        SearchCountResponse response = SearchRestMapper.toResponse(countSearchesUseCase.countBySearchId(searchId));
        log.info("Matching hotel searches counted searchId={} count={}", searchId, response.count());
        return response;
    }
}
