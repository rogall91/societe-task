package org.example.requests.service;

import org.example.requests.domain.Request;
import org.example.requests.domain.RequestNotFoundException;
import org.example.requests.domain.RequestState;
import org.example.requests.domain.StateChange;
import org.example.requests.repository.RequestRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class RequestService {

    private final RequestRepository repository;

    public RequestService(RequestRepository repository) {
        this.repository = repository;
    }

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
        return request;
    }

    public Request verify(Long id) {
        Request request = get(id);
        request.verify();
        return request;
    }

    public Request accept(Long id) {
        Request request = get(id);
        request.accept();
        return request;
    }

    public Request reject(Long id, String reason) {
        Request request = get(id);
        request.reject(reason);
        return request;
    }

    public Request delete(Long id, String reason) {
        Request request = get(id);
        request.delete(reason);
        return request;
    }

    public Request publish(Long id) {
        Request request = get(id);
        request.publish(repository.nextPublicationNumber());
        return request;
    }
}
