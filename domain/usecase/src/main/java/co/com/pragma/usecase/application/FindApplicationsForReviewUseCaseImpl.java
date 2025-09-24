package co.com.pragma.usecase.application;

import co.com.pragma.model.application.Application;
import co.com.pragma.model.application.ApplicationStatus;
import co.com.pragma.model.application.gateways.ApplicationGateway;
import co.com.pragma.model.common.PageRequest;
import co.com.pragma.model.log.gateways.LoggerPort;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@RequiredArgsConstructor
public class FindApplicationsForReviewUseCaseImpl implements FindApplicationsForReviewUseCase {

    private static final List<String> REVIEW_STATUSES = List.of(
            ApplicationStatus.PENDING_REVIEW,
            ApplicationStatus.REJECTED,
            ApplicationStatus.MANUAL_REVIEW
    );

    private final ApplicationGateway applicationGateway;
    private final LoggerPort logger;

    @Override
    public Flux<Application> findApplicationsForReview(final PageRequest pageRequest) {
        this.logger.info("Iniciando búsqueda de solicitudes para revisión. Página: {}, Tamaño: {}",
                pageRequest.page(), pageRequest.size());

        return this.applicationGateway.findByStatusIn(FindApplicationsForReviewUseCaseImpl.REVIEW_STATUSES, pageRequest)
                .doOnComplete(() -> this.logger.info("Búsqueda de solicitudes para revisión completada."));
    }

    @Override
    public Mono<Long> countApplicationsForReview() {
        this.logger.info("Contando el total de solicitudes para revisión.");
        return this.applicationGateway.countByStatusIn(FindApplicationsForReviewUseCaseImpl.REVIEW_STATUSES);
    }
}