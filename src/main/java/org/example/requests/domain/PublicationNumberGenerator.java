package org.example.requests.domain;

/**
 * Supplies unique numeric identifiers for published requests.
 */
@FunctionalInterface
public interface PublicationNumberGenerator {

    long next();
}
