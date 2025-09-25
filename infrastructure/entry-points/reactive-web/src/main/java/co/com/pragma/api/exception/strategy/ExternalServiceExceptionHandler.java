package co.com.pragma.api.exception.strategy;

import co.com.pragma.api.exception.dto.ErrorBody;
import co.com.pragma.api.exception.dto.ErrorResponseWrapper;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientException;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.ConnectException;
import java.util.concurrent.TimeoutException;

/**
 * Maneja excepciones de servicios externos como REST clients, bases de datos, etc.
 */
@Component
public class ExternalServiceExceptionHandler implements ExceptionHandlerStrategy {

    @Override
    public boolean supports(Class<? extends Throwable> type) {
        return WebClientException.class.isAssignableFrom(type) ||
               DataAccessResourceFailureException.class.isAssignableFrom(type) ||
               ConnectException.class.isAssignableFrom(type) ||
               TimeoutException.class.isAssignableFrom(type);
    }

    @Override
    public Mono<ErrorResponseWrapper> handle(Throwable ex, ServerWebExchange exchange) {
        HttpStatus status;
        String message;
        String error;

        if (ex instanceof WebClientResponseException webClientEx) {
            // Error de respuesta HTTP del servicio externo
            status = HttpStatus.SERVICE_UNAVAILABLE;
            message = "Servicio externo no disponible temporalmente";
            error = "External Service Error";

        } else if (ex instanceof WebClientRequestException) {
            // Error de conectividad con servicio externo
            status = HttpStatus.SERVICE_UNAVAILABLE;
            message = "No se pudo conectar con el servicio externo";
            error = "Service Connection Error";

        } else if (ex instanceof DataAccessResourceFailureException) {
            // Error de conectividad con base de datos
            status = HttpStatus.SERVICE_UNAVAILABLE;
            message = "Servicio de base de datos temporalmente no disponible";
            error = "Database Connection Error";

        } else if (ex instanceof ConnectException) {
            // Error de conexión general
            status = HttpStatus.SERVICE_UNAVAILABLE;
            message = "Error de conectividad con servicios externos";
            error = "Connection Error";

        } else if (ex instanceof TimeoutException) {
            // Timeout de servicio
            status = HttpStatus.GATEWAY_TIMEOUT;
            message = "Tiempo de espera agotado al comunicarse con servicios externos";
            error = "Service Timeout";

        } else {
            // Otros errores de servicios externos
            status = HttpStatus.SERVICE_UNAVAILABLE;
            message = "Error temporal en servicios externos";
            error = "External Service Error";
        }

        ErrorBody errorBody = new ErrorBody(
                status.value(),
                error,
                message,
                null
        );

        return Mono.just(new ErrorResponseWrapper(status, errorBody));
    }
}