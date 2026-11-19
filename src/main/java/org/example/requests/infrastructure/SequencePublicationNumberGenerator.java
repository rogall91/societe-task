package org.example.requests.infrastructure;

import lombok.RequiredArgsConstructor;
import org.example.requests.domain.PublicationNumberGenerator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Takes publication numbers from the {@code publication_number_seq} database sequence (H2 syntax).
 */
@Component
@RequiredArgsConstructor
public class SequencePublicationNumberGenerator implements PublicationNumberGenerator {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public long next() {
        return jdbcTemplate.queryForObject("select next value for publication_number_seq", Long.class);
    }
}
