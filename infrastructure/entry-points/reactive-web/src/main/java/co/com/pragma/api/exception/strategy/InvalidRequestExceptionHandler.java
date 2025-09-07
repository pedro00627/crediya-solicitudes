package co.com.pragma.api.exception.strategy;

import co.com.pragma.api.exception.InvalidRequestException;
import co.com.pragma.api.exception.dto.ErrorBody;
import co.com.pragma.api.exception.dto.ErrorResponseWrapper;
import co.com.pragma.model.log.gateways.LoggerPort;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Order(2) // Segunda prioridad
public class InvalidRequestExceptionHandler implements ExceptionHandlerStrategy {

    private final LoggerPort logger;

    public InvalidRequestExceptionHandler(LoggerPort logger) {
        this.logger = logger;
    }

    @Override
    public boolean supports(Class<? extends Throwable> type) {
        return InvalidRequestException.class.isAssignableFrom(type);
    }

    @Override
    public Mono<ErrorResponseWrapper> handle(Throwable ex, ServerWebExchange exchange) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        logger.info("Petición inválida para la ruta [{}]: {}", exchange.getRequest().getPath(), ex.getMessage());
        ErrorBody body = new ErrorBody(status.value(), "Invalid Request", ex.getMessage(), null);
        return Mono.just(new ErrorResponseWrapper(status, body));
    }
}