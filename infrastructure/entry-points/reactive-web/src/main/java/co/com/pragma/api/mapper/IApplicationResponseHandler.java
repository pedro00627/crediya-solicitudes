package co.com.pragma.api.mapper;

import co.com.pragma.model.application.ApplicationCreationResult;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

/**
 * Interfaz para manejar la construcción de respuestas HTTP para las operaciones de solicitud de aplicación.
 * Abstrae la lógica de mapear un resultado de caso de uso a una respuesta de servidor completa.
 */
public interface IApplicationResponseHandler {
    Mono<ServerResponse> buildCreationResponse(ApplicationCreationResult result);
}
