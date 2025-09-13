package co.com.pragma.api;

import co.com.pragma.model.application.ApplicationReviewDTO;
import co.com.pragma.api.mapper.ApplicationMapperAdapter;
import co.com.pragma.model.application.gateways.FindApplicationsForReviewUseCase;
import co.com.pragma.model.common.PageRequest;
import co.com.pragma.model.common.PagedResponse;
import co.com.pragma.model.log.gateways.LoggerPort;
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
    public Mono<ServerResponse> getApplicationsForReview(ServerRequest serverRequest) {
        return extractAndValidatePageRequest(serverRequest)
                .flatMap(pageRequest -> {
                    Mono<Long> totalItemsMono = findApplicationsForReviewUseCase.countApplicationsForReview();

                    // Se delega toda la lógica de enriquecimiento al mapper para que lo haga en lote.
                    var applicationsFlux = findApplicationsForReviewUseCase.findApplicationsForReview(pageRequest);
                    Mono<List<ApplicationReviewDTO>> contentMono = mapper.toEnrichedReviewDTOs(applicationsFlux);

                    return Mono.zip(contentMono, totalItemsMono)
                            .flatMap(tuple -> {
                                var content = tuple.getT1();
                                var totalItems = tuple.getT2();
                                var pagedResponse = PagedResponse.of(content, pageRequest.page(), totalItems, pageRequest.size());

                                return ServerResponse.ok()
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .bodyValue(pagedResponse);
                            });
                });
    }

    private Mono<PageRequest> extractAndValidatePageRequest(ServerRequest serverRequest) {
        Mono<Integer> pageMono = parseQueryParam(serverRequest, "page", 0);
        Mono<Integer> sizeMono = parseQueryParam(serverRequest, "size", 10);

        return Mono.zip(pageMono, sizeMono)
                .flatMap(tuple -> {
                    int page = tuple.getT1();
                    int size = tuple.getT2();
                    logger.info("Recibida solicitud GET /api/v1/solicitud para revisión manual. Page: {}, Size: {}", page, size);

                    if (page < 0 || size <= 0) {
                        return Mono.error(new IllegalArgumentException("Los parámetros de paginación 'page' y 'size' deben ser positivos."));
                    }
                    return Mono.just(new PageRequest(page, size));
                });
    }

    private Mono<Integer> parseQueryParam(ServerRequest request, String paramName, int defaultValue) {
        return Mono.just(request.queryParam(paramName).orElse(String.valueOf(defaultValue)))
                .map(Integer::parseInt)
                .onErrorMap(NumberFormatException.class, e -> new IllegalArgumentException("El parámetro '" + paramName + "' debe ser un número entero válido."));
    }
}