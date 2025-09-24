package co.com.pragma.api.exception.strategy;

import co.com.pragma.api.exception.dto.ErrorBody;
import co.com.pragma.api.exception.dto.ErrorResponseWrapper;
import co.com.pragma.model.exception.BusinessException;
import co.com.pragma.model.log.gateways.LoggerPort;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Order(1) // Damos alta prioridad a las excepciones de negocio
public class BusinessExceptionHandler implements ExceptionHandlerStrategy {

    private final LoggerPort logger;

    public BusinessExceptionHandler(final LoggerPort logger) {
        this.logger = logger;
    }

    @Override
    public boolean supports(final Class<? extends Throwable> type) {
        return BusinessException.class.isAssignableFrom(type);
    }

    @Override
    public Mono<ErrorResponseWrapper> handle(final Throwable ex, final ServerWebExchange exchange) {
        final HttpStatus status = HttpStatus.CONFLICT;
        // LoggerPort no tiene el nivel WARN, se usa INFO para registrar el evento.
        this.logger.warn("Violación de regla de negocio para la petición [{}]: {}", exchange.getRequest().getPath(), ex.getMessage());

        final ErrorBody body = new ErrorBody(status.value(), "Business Rule Violation", ex.getMessage(), null);

        return Mono.just(new ErrorResponseWrapper(status, body));
    }
}
