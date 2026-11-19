package org.example.requests.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Audit log entry: one state change of a request.
 */
@Entity
@Table(name = "request_state_changes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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

    StateChange(Request request, RequestState fromState, RequestState toState, String reason) {
        this.request = request;
        this.fromState = fromState;
        this.toState = toState;
        this.reason = reason;
        this.changedAt = Instant.now();
    }
}
