package org.example.requests.service;

import lombok.RequiredArgsConstructor;
import org.example.requests.domain.PublicationNumberGenerator;
import org.example.requests.domain.Request;
import org.example.requests.domain.RequestNotFoundException;
import org.example.requests.domain.RequestRepository;
import org.example.requests.domain.RequestState;
import org.example.requests.domain.StateChange;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class RequestService {

    private final RequestRepository repository;
    private final PublicationNumberGenerator publicationNumberGenerator;

    public Request create(String name, String content) {
        return repository.save(new Request(name, content));
    }

    @Transactional(readOnly = true)
    public Request get(Long id) {
        return repository.findById(id).orElseThrow(() -> new RequestNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public List<StateChange> history(Long id) {
        return List.copyOf(get(id).getHistory());
    }

    @Transactional(readOnly = true)
    public Page<Request> search(String name, RequestState state, Pageable pageable) {
        return repository.search(name, state, pageable);
    }

    public Request editContent(Long id, String content) {
        Request request = get(id);
        request.editContent(content);
        return repository.save(request);
    }

    public Request verify(Long id) {
        Request request = get(id);
        request.verify();
        return repository.save(request);
    }

    public Request accept(Long id) {
        Request request = get(id);
        request.accept();
        return repository.save(request);
    }

    public Request reject(Long id, String reason) {
        Request request = get(id);
        request.reject(reason);
        return repository.save(request);
    }

    public Request delete(Long id, String reason) {
        Request request = get(id);
        request.delete(reason);
        return repository.save(request);
    }

    public Request publish(Long id) {
        Request request = get(id);
        request.publish(publicationNumberGenerator);
        return repository.save(request);
    }
}
