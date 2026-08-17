package com.riu.hotelsearch.application.port.in;

import com.riu.hotelsearch.domain.model.SearchCriteria;

import java.util.UUID;

public interface RegisterSearchUseCase {
    UUID register(SearchCriteria criteria);
}

