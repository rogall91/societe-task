package org.example.requests.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "requests")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Request {

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, length = 10000)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestState state;

    private String reason;

    @Column(unique = true)
    private Long publicationNumber;

    @Version
    private Long version;

    @OneToMany(mappedBy = "request", cascade = CascadeType.ALL)
    @OrderBy("id")
    private List<StateChange> history = new ArrayList<>();

    public Request(String name, String content) {
        this.name = requireText(name, "name");
        this.content = requireText(content, "content");
        this.state = RequestState.CREATED;
        history.add(new StateChange(this, null, RequestState.CREATED, null));
    }

    public void editContent(String content) {
        if (!state.isContentEditable()) {
            throw new InvalidStateException("Content cannot be edited in state " + state);
        }
        this.content = requireText(content, "content");
    }

    public void verify() {
        moveTo(RequestState.VERIFIED, null);
    }

    public void accept() {
        moveTo(RequestState.ACCEPTED, null);
    }

    public void reject(String reason) {
        moveTo(RequestState.REJECTED, requireText(reason, "reason"));
    }

    public void delete(String reason) {
        moveTo(RequestState.DELETED, requireText(reason, "reason"));
    }

    public void publish(PublicationNumberGenerator generator) {
        moveTo(RequestState.PUBLISHED, null);
        this.publicationNumber = generator.next();
    }

    private void moveTo(RequestState target, String reason) {
        if (!state.canMoveTo(target)) {
            throw new InvalidStateException("Cannot move request from " + state + " to " + target);
        }
        history.add(new StateChange(this, state, target, reason));
        this.state = target;
        if (reason != null) {
            this.reason = reason;
        }
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return value;
    }

    public List<StateChange> getHistory() {
        return Collections.unmodifiableList(history);
    }
}
