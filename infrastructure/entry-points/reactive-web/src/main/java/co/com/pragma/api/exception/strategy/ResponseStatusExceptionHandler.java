package co.com.pragma.api.exception.strategy;

import co.com.pragma.api.exception.dto.ErrorBody;
import co.com.pragma.api.exception.dto.ErrorResponseWrapper;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

// Se le da un nombre único al bean para evitar conflictos con el que provee Spring Boot por defecto.
@Component("customResponseStatusExceptionHandler")
@Order(Ordered.HIGHEST_PRECEDENCE + 10) // Alta prioridad para manejar estas excepciones específicas antes que el DefaultHandler
public class ResponseStatusExceptionHandler implements ExceptionHandlerStrategy {

    @Override
    public boolean supports(Class<? extends Throwable> type) {
        // Esta estrategia es responsable de todas las excepciones ResponseStatusException y sus subclases.
        return ResponseStatusException.class.isAssignableFrom(type);
    }

    @Override
    public Mono<ErrorResponseWrapper> handle(Throwable ex, ServerWebExchange exchange) {
        ResponseStatusException exception = (ResponseStatusException) ex;
        HttpStatus status = (HttpStatus) exception.getStatusCode();

        // Usamos el 'reason' de la excepción, que es el mensaje que le pasamos al construirla.
        String message = exception.getReason();

        ErrorBody body = new ErrorBody(status.value(), status.getReasonPhrase(), message, null);
        return Mono.just(new ErrorResponseWrapper(status, body));
    }
}
