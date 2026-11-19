package org.example.requests.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.Instant;

/**
 * Audit log entry: one state change of a request. {@code fromState} is null for the creation.
 */
@Getter
@RequiredArgsConstructor
public class StateChange {

    private final RequestState fromState;
    private final RequestState toState;
    private final String reason;
    private final Instant changedAt;

    StateChange(RequestState fromState, RequestState toState, String reason) {
        this(fromState, toState, reason, Instant.now());
    }
}
