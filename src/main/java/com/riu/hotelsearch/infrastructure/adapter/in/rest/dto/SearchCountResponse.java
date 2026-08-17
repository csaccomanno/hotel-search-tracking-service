package com.riu.hotelsearch.infrastructure.adapter.in.rest.dto;

import java.util.UUID;

public record SearchCountResponse(UUID searchId, SearchDetailsResponse search, long count) {
}
