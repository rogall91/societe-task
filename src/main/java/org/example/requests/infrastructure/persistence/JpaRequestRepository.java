package org.example.requests.infrastructure.persistence;

import lombok.RequiredArgsConstructor;
import org.example.requests.domain.Request;
import org.example.requests.domain.RequestRepository;
import org.example.requests.domain.RequestState;
import org.example.requests.domain.StateChange;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Stores domain {@link Request}s as JPA entities and maps between the two models.
 */
@Repository
@Transactional
@RequiredArgsConstructor
class JpaRequestRepository implements RequestRepository {

    private final RequestJpaRepository jpa;

    @Override
    public Request save(Request request) {
        // A request that was never stored has no version yet.
        RequestEntity entity = request.getVersion() == null ? newEntity(request) : loadForUpdate(request);
        entity.name = request.getName();
        entity.content = request.getContent();
        entity.state = request.getState();
        entity.reason = request.getReason();
        entity.publicationNumber = request.getPublicationNumber();

        // History is append-only: store only the entries that are not stored yet.
        List<StateChange> history = request.getHistory();
        for (int i = entity.history.size(); i < history.size(); i++) {
            entity.history.add(toEntity(history.get(i), entity));
        }
        return toDomain(jpa.saveAndFlush(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Request> findById(UUID id) {
        return jpa.findById(id).map(JpaRequestRepository::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Request> search(String name, RequestState state, Pageable pageable) {
        return jpa.search(name, state, pageable).map(JpaRequestRepository::toDomain);
    }

    private static RequestEntity newEntity(Request request) {
        RequestEntity entity = new RequestEntity();
        entity.id = request.getId();
        entity.createdAt = Instant.now();
        return entity;
    }

    private RequestEntity loadForUpdate(Request request) {
        RequestEntity entity = jpa.findById(request.getId())
                .orElseThrow(() -> new ObjectOptimisticLockingFailureException(RequestEntity.class, request.getId()));
        if (!Objects.equals(entity.version, request.getVersion())) {
            throw new ObjectOptimisticLockingFailureException(RequestEntity.class, request.getId());
        }
        return entity;
    }

    private static StateChangeEntity toEntity(StateChange change, RequestEntity request) {
        StateChangeEntity entity = new StateChangeEntity();
        entity.request = request;
        entity.fromState = change.getFromState();
        entity.toState = change.getToState();
        entity.reason = change.getReason();
        entity.changedAt = change.getChangedAt();
        return entity;
    }

    private static Request toDomain(RequestEntity entity) {
        List<StateChange> history = entity.history.stream()
                .map(c -> new StateChange(c.fromState, c.toState, c.reason, c.changedAt))
                .toList();
        return Request.restore(entity.id, entity.name, entity.content, entity.state, entity.reason,
                entity.publicationNumber, entity.version, history);
    }
}
