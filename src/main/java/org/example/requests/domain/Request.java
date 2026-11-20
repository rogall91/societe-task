package org.example.requests.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * A request and its business rules. Plain Java: persistence is handled by {@link RequestRepository}.
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Request {

    public static final int MAX_NAME_LENGTH = 255;
    public static final int MAX_CONTENT_LENGTH = 10_000;
    public static final int MAX_REASON_LENGTH = 255;

    private final UUID id;
    private final String name;
    private String content;
    private RequestState state;
    private String reason;
    private Long publicationNumber;
    private final Long version;
    private final List<StateChange> history;

    public Request(String name, String content) {
        this(UUID.randomUUID(), requireText(name, "name", MAX_NAME_LENGTH),
                requireText(content, "content", MAX_CONTENT_LENGTH), RequestState.CREATED,
                null, null, null, new ArrayList<>());
        history.add(new StateChange(null, RequestState.CREATED, null));
    }

    /**
     * Rebuilds a request loaded from storage.
     */
    public static Request restore(UUID id, String name, String content, RequestState state, String reason,
                                  Long publicationNumber, Long version, List<StateChange> history) {
        return new Request(id, name, content, state, reason, publicationNumber, version, new ArrayList<>(history));
    }

    public void editContent(String content) {
        if (!state.isContentEditable()) {
            throw new InvalidStateException("Content cannot be edited in state " + state);
        }
        this.content = requireText(content, "content", MAX_CONTENT_LENGTH);
    }

    public void verify() {
        moveTo(RequestState.VERIFIED, null);
    }

    public void accept() {
        moveTo(RequestState.ACCEPTED, null);
    }

    public void reject(String reason) {
        moveTo(RequestState.REJECTED, requireText(reason, "reason", MAX_REASON_LENGTH));
    }

    public void delete(String reason) {
        moveTo(RequestState.DELETED, requireText(reason, "reason", MAX_REASON_LENGTH));
    }

    public void publish(PublicationNumberGenerator generator) {
        moveTo(RequestState.PUBLISHED, null);
        this.publicationNumber = generator.next();
    }

    private void moveTo(RequestState target, String reason) {
        if (!state.canMoveTo(target)) {
            throw new InvalidStateException("Cannot move request from " + state + " to " + target);
        }
        history.add(new StateChange(state, target, reason));
        this.state = target;
        if (reason != null) {
            this.reason = reason;
        }
    }

    private static String requireText(String value, String field, int maxLength) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        if (value.length() > maxLength) {
            throw new IllegalArgumentException(field + " must be at most " + maxLength + " characters");
        }
        return value;
    }

    public List<StateChange> getHistory() {
        return Collections.unmodifiableList(history);
    }
}
