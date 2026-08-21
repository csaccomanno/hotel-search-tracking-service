package com.riu.hotelsearch.infrastructure.adapter.in.rest;

import com.riu.hotelsearch.application.exception.SearchNotFoundException;
import com.riu.hotelsearch.application.port.in.CountSearchesUseCase;
import com.riu.hotelsearch.application.port.in.RegisterSearchUseCase;
import com.riu.hotelsearch.infrastructure.adapter.in.rest.dto.ApiErrorResponse;
import com.riu.hotelsearch.infrastructure.adapter.in.rest.dto.SearchCountResponse;
import com.riu.hotelsearch.infrastructure.adapter.in.rest.dto.SearchIdResponse;
import com.riu.hotelsearch.infrastructure.adapter.in.rest.dto.SearchRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping
@RequiredArgsConstructor
@Tag(name = "Hotel searches")
public class HotelSearchController {

    private final RegisterSearchUseCase registerSearchUseCase;
    private final CountSearchesUseCase countSearchesUseCase;

    @PostMapping("/search")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register a hotel search")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Search registered"),
            @ApiResponse(responseCode = "400", description = "Invalid request",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public SearchIdResponse register(@Valid @RequestBody SearchRequest request) {
        UUID searchId = registerSearchUseCase.register(SearchRestMapper.toDomain(request));
        return new SearchIdResponse(searchId);
    }

    @GetMapping("/count")
    @Operation(summary = "Count matching hotel searches")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Search count returned"),
            @ApiResponse(responseCode = "404", description = "Search not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public SearchCountResponse count(@RequestParam String searchId) {
        UUID parsedSearchId = parseSearchId(searchId);
        return SearchRestMapper.toResponse(countSearchesUseCase.countBySearchId(parsedSearchId));
    }

    private UUID parseSearchId(String searchId) {
        try {
            return UUID.fromString(searchId);
        } catch (IllegalArgumentException exception) {
            throw new SearchNotFoundException(searchId);
        }
    }
}
