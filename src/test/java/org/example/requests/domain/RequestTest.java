package org.example.requests.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RequestTest {

    @Test
    void newRequestIsCreatedWithHistoryEntry() {
        Request request = new Request("name", "body");

        assertThat(request.getState()).isEqualTo(RequestState.CREATED);
        assertThat(request.getHistory()).hasSize(1);
        assertThat(request.getHistory().get(0).getToState()).isEqualTo(RequestState.CREATED);
    }

    @Test
    void nameAndContentAreMandatory() {
        assertThatThrownBy(() -> new Request(" ", "body")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Request("name", null)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void happyPathToPublished() {
        Request request = new Request("name", "body");
        request.verify();
        request.accept();
        request.publish(7);

        assertThat(request.getState()).isEqualTo(RequestState.PUBLISHED);
        assertThat(request.getPublicationNumber()).isEqualTo(7);
        assertThat(request.getHistory()).extracting(StateChange::getToState).containsExactly(
                RequestState.CREATED, RequestState.VERIFIED, RequestState.ACCEPTED, RequestState.PUBLISHED);
    }

    @Test
    void contentEditableOnlyInCreatedAndVerified() {
        Request request = new Request("name", "body");
        request.editContent("v2");
        request.verify();
        request.editContent("v3");
        assertThat(request.getContent()).isEqualTo("v3");

        request.accept();
        assertThatThrownBy(() -> request.editContent("v4")).isInstanceOf(InvalidStateException.class);
    }

    @Test
    void deleteAndRejectRequireReason() {
        Request created = new Request("name", "body");
        assertThatThrownBy(() -> created.delete(" ")).isInstanceOf(IllegalArgumentException.class);
        created.delete("duplicate");
        assertThat(created.getState()).isEqualTo(RequestState.DELETED);
        assertThat(created.getReason()).isEqualTo("duplicate");

        Request verified = new Request("name", "body");
        verified.verify();
        assertThatThrownBy(() -> verified.reject(null)).isInstanceOf(IllegalArgumentException.class);
        verified.reject("incomplete");
        assertThat(verified.getState()).isEqualTo(RequestState.REJECTED);
        assertThat(verified.getHistory().get(2).getReason()).isEqualTo("incomplete");
    }

    @Test
    void acceptedRequestCanBeRejected() {
        Request request = new Request("name", "body");
        request.verify();
        request.accept();
        request.reject("late");
        assertThat(request.getState()).isEqualTo(RequestState.REJECTED);
    }

    @Test
    void transitionsOutsideTheDiagramAreRefused() {
        Request request = new Request("name", "body");
        assertThatThrownBy(request::accept).isInstanceOf(InvalidStateException.class);
        assertThatThrownBy(() -> request.publish(1)).isInstanceOf(InvalidStateException.class);
        assertThatThrownBy(() -> request.reject("x")).isInstanceOf(InvalidStateException.class);

        request.verify();
        assertThatThrownBy(() -> request.delete("x")).isInstanceOf(InvalidStateException.class);
        assertThatThrownBy(request::verify).isInstanceOf(InvalidStateException.class);

        request.accept();
        assertThatThrownBy(() -> request.delete("x")).isInstanceOf(InvalidStateException.class);
        assertThatThrownBy(request::verify).isInstanceOf(InvalidStateException.class);
        assertThatThrownBy(request::accept).isInstanceOf(InvalidStateException.class);
    }

    @Test
    void finalStatesAllowNoTransitions() {
        for (RequestState finalState : new RequestState[]{RequestState.PUBLISHED, RequestState.REJECTED, RequestState.DELETED}) {
            for (RequestState target : RequestState.values()) {
                assertThat(finalState.canMoveTo(target)).isFalse();
            }
        }
    }
}
