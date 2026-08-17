package com.riu.hotelsearch.application.port.in;

import com.riu.hotelsearch.application.model.SearchCountResult;

import java.util.UUID;

public interface CountSearchesUseCase {
    SearchCountResult countBySearchId(UUID searchId);
}

