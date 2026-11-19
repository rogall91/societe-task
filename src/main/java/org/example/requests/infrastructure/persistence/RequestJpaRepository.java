package org.example.requests.infrastructure.persistence;

import org.example.requests.domain.RequestState;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

interface RequestJpaRepository extends JpaRepository<RequestEntity, Long> {

    @Query("""
            select r from RequestEntity r
            where (:name is null or lower(r.name) like lower(concat('%', :name, '%')))
              and (:state is null or r.state = :state)
            """)
    Page<RequestEntity> search(String name, RequestState state, Pageable pageable);
}
