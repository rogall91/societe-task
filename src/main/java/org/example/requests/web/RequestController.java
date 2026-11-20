package org.example.requests.web;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.example.requests.domain.Request;
import org.example.requests.domain.RequestState;
import org.example.requests.domain.StateChange;
import org.example.requests.service.RequestService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/requests")
@RequiredArgsConstructor
public class RequestController {

    private final RequestService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RequestDto create(@Valid @RequestBody CreateBody body) {
        return RequestDto.from(service.create(body.name(), body.content()));
    }

    @GetMapping
    public Page<RequestDto> list(@RequestParam(required = false) String name,
                                 @RequestParam(required = false) RequestState state,
                                 @PageableDefault(size = 10) Pageable pageable) {
        return service.search(name, state, pageable).map(RequestDto::from);
    }

    @GetMapping("/{id}")
    public RequestDto get(@PathVariable UUID id) {
        return RequestDto.from(service.get(id));
    }

    @GetMapping("/{id}/history")
    public List<HistoryDto> history(@PathVariable UUID id) {
        return service.history(id).stream().map(HistoryDto::from).toList();
    }

    @PutMapping("/{id}/content")
    public RequestDto editContent(@PathVariable UUID id, @Valid @RequestBody ContentBody body) {
        return RequestDto.from(service.editContent(id, body.content()));
    }

    @PostMapping("/{id}/verify")
    public RequestDto verify(@PathVariable UUID id) {
        return RequestDto.from(service.verify(id));
    }

    @PostMapping("/{id}/accept")
    public RequestDto accept(@PathVariable UUID id) {
        return RequestDto.from(service.accept(id));
    }

    @PostMapping("/{id}/publish")
    public RequestDto publish(@PathVariable UUID id) {
        return RequestDto.from(service.publish(id));
    }

    @PostMapping("/{id}/reject")
    public RequestDto reject(@PathVariable UUID id, @Valid @RequestBody ReasonBody body) {
        return RequestDto.from(service.reject(id, body.reason()));
    }

    @PostMapping("/{id}/delete")
    public RequestDto delete(@PathVariable UUID id, @Valid @RequestBody ReasonBody body) {
        return RequestDto.from(service.delete(id, body.reason()));
    }

    public record CreateBody(@NotBlank String name, @NotBlank String content) {
    }

    public record ContentBody(@NotBlank String content) {
    }

    public record ReasonBody(@NotBlank String reason) {
    }

    public record RequestDto(UUID id, String name, String content, RequestState state, String reason,
                             Long publicationNumber) {
        static RequestDto from(Request r) {
            return new RequestDto(r.getId(), r.getName(), r.getContent(), r.getState(), r.getReason(),
                    r.getPublicationNumber());
        }
    }

    public record HistoryDto(RequestState fromState, RequestState toState, String reason, Instant changedAt) {
        static HistoryDto from(StateChange c) {
            return new HistoryDto(c.getFromState(), c.getToState(), c.getReason(), c.getChangedAt());
        }
    }
}
