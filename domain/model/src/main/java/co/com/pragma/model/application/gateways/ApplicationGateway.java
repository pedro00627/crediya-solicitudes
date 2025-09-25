package co.com.pragma.model.application.gateways;

import co.com.pragma.model.application.Application;
import co.com.pragma.model.common.PageRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

public interface ApplicationGateway {
    Mono<Application> save(Application application);

    Mono<Application> findById(UUID applicationId);

    Flux<Application> findOpenApplicationsByDocumentId(String documentId, List<Integer> statusIds);

    Flux<Application> findByStatusIn(List<String> statuses, PageRequest pageRequest);

    Mono<Long> countByStatusIn(List<String> statuses);
}