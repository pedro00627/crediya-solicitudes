package co.com.pragma.model.application.gateways;

import co.com.pragma.model.application.Application;
import co.com.pragma.model.common.PageRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface FindApplicationsForReviewUseCase {
    Flux<Application> findApplicationsForReview(PageRequest pageRequest);
    Mono<Long> countApplicationsForReview();
}