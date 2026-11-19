package org.example.requests.web;

import org.example.requests.domain.Request;
import org.example.requests.domain.RequestState;
import org.example.requests.repository.RequestRepository;
import org.example.requests.service.RequestService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.ProblemDetail;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class ConcurrentUpdateTest {

    @Autowired
    private RequestService service;

    @Autowired
    private RequestRepository repository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Test
    void concurrentChangeOfTheSameRequestIsRejected() {
        Long id = service.create("name", "body").getId();
        TransactionTemplate first = new TransactionTemplate(transactionManager);
        TransactionTemplate second = new TransactionTemplate(transactionManager);
        second.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        // The first transaction reads the request, then another transaction changes it and commits first.
        assertThatThrownBy(() -> first.executeWithoutResult(status -> {
            Request request = repository.findById(id).orElseThrow();
            second.executeWithoutResult(inner -> service.delete(id, "duplicate"));
            request.verify();
        })).isInstanceOf(OptimisticLockingFailureException.class);

        assertThat(service.get(id).getState()).isEqualTo(RequestState.DELETED);
    }

    @Test
    void optimisticLockFailureIsMappedToConflict() {
        ProblemDetail problem = new ErrorHandler()
                .concurrentUpdate(new OptimisticLockingFailureException("stale"));

        assertThat(problem.getStatus()).isEqualTo(409);
    }
}
