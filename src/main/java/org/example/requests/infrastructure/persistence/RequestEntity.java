package org.example.requests.infrastructure.persistence;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import org.example.requests.domain.RequestState;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "requests")
class RequestEntity {

    @Id
    UUID id;

    /** Used only to order the list newest first; not part of the domain model. */
    @Column(nullable = false, updatable = false)
    Instant createdAt;

    @Column(nullable = false)
    String name;

    @Column(nullable = false, length = 10000)
    String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    RequestState state;

    String reason;

    @Column(unique = true)
    Long publicationNumber;

    @Version
    Long version;

    @OneToMany(mappedBy = "request", cascade = CascadeType.ALL)
    @OrderBy("id")
    List<StateChangeEntity> history = new ArrayList<>();
}
