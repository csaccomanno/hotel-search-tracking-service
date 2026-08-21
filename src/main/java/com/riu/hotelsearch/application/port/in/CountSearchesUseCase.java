package com.riu.hotelsearch.application.port.in;

import com.riu.hotelsearch.domain.model.SearchCountResult;

import java.util.UUID;

public interface CountSearchesUseCase {
    SearchCountResult countBySearchId(UUID searchId);
}
