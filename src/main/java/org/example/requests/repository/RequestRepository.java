package org.example.requests.repository;

import org.example.requests.domain.Request;
import org.example.requests.domain.RequestState;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface RequestRepository extends JpaRepository<Request, Long> {

    @Query("""
            select r from Request r
            where (:name is null or lower(r.name) like lower(concat('%', :name, '%')))
              and (:state is null or r.state = :state)
            """)
    Page<Request> search(String name, RequestState state, Pageable pageable);
}
