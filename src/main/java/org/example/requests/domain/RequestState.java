package org.example.requests.domain;

/**
 * Request states and the transitions permitted by the state diagram.
 */
public enum RequestState {
    CREATED,
    VERIFIED,
    ACCEPTED,
    PUBLISHED,
    REJECTED,
    DELETED;

    public boolean canMoveTo(RequestState target) {
        return switch (this) {
            case CREATED -> target == VERIFIED || target == DELETED;
            case VERIFIED -> target == ACCEPTED || target == REJECTED;
            case ACCEPTED -> target == PUBLISHED || target == REJECTED;
            case PUBLISHED, REJECTED, DELETED -> false;
        };
    }

    public boolean isContentEditable() {
        return this == CREATED || this == VERIFIED;
    }
}
