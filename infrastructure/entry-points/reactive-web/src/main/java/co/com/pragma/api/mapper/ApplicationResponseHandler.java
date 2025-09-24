package co.com.pragma.api.mapper;

import co.com.pragma.model.application.ApplicationCreationResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

/**
 * Implementación concreta para construir respuestas HTTP para las operaciones de solicitud de aplicación.
 */
@Component
@RequiredArgsConstructor
public class ApplicationResponseHandler implements IApplicationResponseHandler {

    private final IApplicationMapper responseMapper;

    @Override
    public Mono<ServerResponse> buildCreationResponse(final ApplicationCreationResult result) {
        return Mono.just(result)
                .map(this.responseMapper::toResponse)
                .flatMap(response -> ServerResponse
                        .status(HttpStatus.CREATED)
                        .bodyValue(response));
    }
}
