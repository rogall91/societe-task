package org.example.requests.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

/**
 * Storage for requests. Implemented in the infrastructure layer.
 */
public interface RequestRepository {

    /**
     * Stores a new or changed request and returns it as stored (with its new version).
     */
    Request save(Request request);

    Optional<Request> findById(UUID id);

    /**
     * Finds requests whose name contains {@code name} (case-insensitive) and that are in {@code state};
     * null criteria are ignored.
     */
    Page<Request> search(String name, RequestState state, Pageable pageable);
}
