package co.com.pragma.model.application.gateways;

import co.com.pragma.model.application.Application;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface ApplicationRepository {
    Mono<Application> save(Application application);
    Flux<Application> findOpenApplicationsByDocumentId(String documentId, List<Integer> statusIds);
}