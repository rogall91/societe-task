package org.example.requests.infrastructure.persistence;

import org.example.requests.domain.RequestState;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

interface RequestJpaRepository extends JpaRepository<RequestEntity, UUID> {

    @Query("""
            select r from RequestEntity r
            where (:name is null or lower(r.name) like lower(concat('%', :name, '%')))
              and (:state is null or r.state = :state)
            order by r.createdAt desc, r.id
            """)
    Page<RequestEntity> search(String name, RequestState state, Pageable pageable);
}
