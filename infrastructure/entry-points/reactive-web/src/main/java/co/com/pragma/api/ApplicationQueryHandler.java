package co.com.pragma.api;

import co.com.pragma.api.dto.ApplicationReviewDTO;
import co.com.pragma.api.mapper.ApplicationMapperAdapter;
import co.com.pragma.model.common.PageRequest;
import co.com.pragma.model.common.PagedResponse;
import co.com.pragma.model.log.gateways.LoggerPort;
import co.com.pragma.usecase.application.FindApplicationsForReviewUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ApplicationQueryHandler implements IApplicationQueryApi {

    private static final String UNKNOWN_VALUE = "Desconocido";

    private final FindApplicationsForReviewUseCase findApplicationsForReviewUseCase;
    private final ApplicationMapperAdapter mapper;
    private final LoggerPort logger;

    @Override
    public Mono<ServerResponse> getApplicationsForReview(final ServerRequest serverRequest) {
        return this.extractAndValidatePageRequest(serverRequest)
                .flatMap(pageRequest -> {
                    final Mono<Long> totalItemsMono = this.findApplicationsForReviewUseCase.countApplicationsForReview();

                    // Se delega toda la lógica de enriquecimiento al mapper para que lo haga en lote.
                    final var applicationsFlux = this.findApplicationsForReviewUseCase.findApplicationsForReview(pageRequest);
                    final Mono<List<ApplicationReviewDTO>> contentMono = this.mapper.toEnrichedReviewDTOs(applicationsFlux);

                    return Mono.zip(contentMono, totalItemsMono)
                            .flatMap(tuple -> {
                                final var content = tuple.getT1();
                                final var totalItems = tuple.getT2();
                                final var pagedResponse = PagedResponse.of(content, pageRequest.page(), totalItems, pageRequest.size());

                                return ServerResponse.ok()
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .bodyValue(pagedResponse);
                            });
                });
    }

    private Mono<PageRequest> extractAndValidatePageRequest(final ServerRequest serverRequest) {
        final Mono<Integer> pageMono = this.parseQueryParam(serverRequest, "page", 0);
        final Mono<Integer> sizeMono = this.parseQueryParam(serverRequest, "size", 10);

        return Mono.zip(pageMono, sizeMono)
                .flatMap(tuple -> {
                    final int page = tuple.getT1();
                    final int size = tuple.getT2();
                    this.logger.info("Recibida solicitud GET /api/v1/solicitud para revisión manual. Page: {}, Size: {}", page, size);

                    if (0 > page || 0 >= size) {
                        return Mono.error(new IllegalArgumentException("Los parámetros de paginación 'page' y 'size' deben ser positivos."));
                    }
                    return Mono.just(new PageRequest(page, size));
                });
    }

    private Mono<Integer> parseQueryParam(final ServerRequest request, final String paramName, final int defaultValue) {
        return Mono.just(request.queryParam(paramName).orElse(String.valueOf(defaultValue)))
                .map(Integer::parseInt)
                .onErrorMap(NumberFormatException.class, e -> new IllegalArgumentException("El parámetro '" + paramName + "' debe ser un número entero válido."));
    }
}