package co.com.pragma.api.exception.strategy;

import co.com.pragma.api.exception.dto.ErrorBody;
import co.com.pragma.api.exception.dto.ErrorResponseWrapper;
import co.com.pragma.model.log.gateways.LoggerPort;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Order(Ordered.LOWEST_PRECEDENCE) // La prioridad más baja para que sea el último recurso
public class DefaultExceptionHandler implements ExceptionHandlerStrategy {

    private final LoggerPort logger;

    public DefaultExceptionHandler(LoggerPort logger) {
        this.logger = logger;
    }

    @Override
    public boolean supports(Class<? extends Throwable> type) {
        return true; // Atrapa todos los errores que los otros no atraparon
    }

    @Override
    public Mono<ErrorResponseWrapper> handle(Throwable ex, ServerWebExchange exchange) {
        // Logueamos el error con el stack trace completo para un diagnóstico detallado
        String errorMessage = String.format("Error inesperado para la petición [%s]: %s", exchange.getRequest().getPath(), ex.getMessage());
        logger.error(errorMessage, ex);

        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        ErrorBody body = new ErrorBody(status.value(), "Internal Server Error", "Ocurrió un error inesperado.", null);
        return Mono.just(new ErrorResponseWrapper(status, body));
    }
}