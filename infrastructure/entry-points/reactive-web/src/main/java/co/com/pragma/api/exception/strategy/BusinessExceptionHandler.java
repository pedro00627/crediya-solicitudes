package co.com.pragma.api.exception.strategy;

import co.com.pragma.api.exception.dto.ErrorBody;
import co.com.pragma.api.exception.dto.ErrorResponseWrapper;
import co.com.pragma.model.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Order(1) // Damos alta prioridad a las excepciones de negocio
@Slf4j
public class BusinessExceptionHandler implements ExceptionHandlerStrategy {

    @Override
    public boolean supports(Class<? extends Throwable> type) {
        return BusinessException.class.isAssignableFrom(type);
    }

    @Override
    public Mono<ErrorResponseWrapper> handle(Throwable ex, ServerWebExchange exchange) {
        HttpStatus status = HttpStatus.CONFLICT;
        log.warn("Violación de regla de negocio para la petición [{}]: {}", exchange.getRequest().getPath(), ex.getMessage()); // Log con WARN porque no es un error del sistema

        ErrorBody body = new ErrorBody(status.value(), "Business Rule Violation", ex.getMessage(), null);

        return Mono.just(new ErrorResponseWrapper(status, body));
    }
}