package com.riu.hotelsearch.application.model;

import com.riu.hotelsearch.domain.model.HotelSearch;

public record SearchCountResult(HotelSearch search, long count) {
}

