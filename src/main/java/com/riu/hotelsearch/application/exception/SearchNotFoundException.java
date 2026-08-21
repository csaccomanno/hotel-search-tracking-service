package com.riu.hotelsearch.application.exception;

import java.util.UUID;

public class SearchNotFoundException extends RuntimeException {

    public SearchNotFoundException(UUID searchId) {
        this(searchId.toString());
    }

    public SearchNotFoundException(String searchId) {
        super("Search not found: " + searchId);
    }
}
