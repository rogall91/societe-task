package org.example.requests.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * Audit log entry: one state change of a request.
 */
@Entity
@Table(name = "request_state_changes")
public class StateChange {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(optional = false)
    private Request request;

    @Enumerated(EnumType.STRING)
    private RequestState fromState;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestState toState;

    private String reason;

    @Column(nullable = false)
    private Instant changedAt;

    protected StateChange() {
    }

    StateChange(Request request, RequestState fromState, RequestState toState, String reason) {
        this.request = request;
        this.fromState = fromState;
        this.toState = toState;
        this.reason = reason;
        this.changedAt = Instant.now();
    }

    public RequestState getFromState() {
        return fromState;
    }

    public RequestState getToState() {
        return toState;
    }

    public String getReason() {
        return reason;
    }

    public Instant getChangedAt() {
        return changedAt;
    }
}
