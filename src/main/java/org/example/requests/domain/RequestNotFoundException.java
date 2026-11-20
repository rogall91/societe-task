package org.example.requests.domain;

import java.util.UUID;

public class RequestNotFoundException extends RuntimeException {

    public RequestNotFoundException(UUID id) {
        super("Request " + id + " not found");
    }
}
