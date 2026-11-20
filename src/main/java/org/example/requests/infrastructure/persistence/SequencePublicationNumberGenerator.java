package org.example.requests.infrastructure.persistence;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.example.requests.domain.PublicationNumberGenerator;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Issues publication numbers by storing a {@link PublicationNumberEntity}; its generated id is the number.
 */
@Component
@Transactional
@RequiredArgsConstructor
class SequencePublicationNumberGenerator implements PublicationNumberGenerator {

    private final EntityManager entityManager;

    @Override
    public long next() {
        PublicationNumberEntity issued = new PublicationNumberEntity();
        entityManager.persist(issued);
        return issued.number;
    }
}
