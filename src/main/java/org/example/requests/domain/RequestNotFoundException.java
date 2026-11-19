package org.example.requests.domain;

public class RequestNotFoundException extends RuntimeException {

    public RequestNotFoundException(Long id) {
        super("Request " + id + " not found");
    }
}
