package org.example.requests.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.example.requests.domain.RequestState;

import java.time.Instant;

@Entity
@Table(name = "request_state_changes")
class StateChangeEntity {

    @Id
    @GeneratedValue
    Long id;

    @ManyToOne(optional = false)
    RequestEntity request;

    @Enumerated(EnumType.STRING)
    RequestState fromState;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    RequestState toState;

    String reason;

    @Column(nullable = false)
    Instant changedAt;
}
