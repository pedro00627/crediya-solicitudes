package co.com.pragma.api.exception.strategy;

import co.com.pragma.api.exception.dto.ErrorBody;
import co.com.pragma.api.exception.dto.ErrorResponseWrapper;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Order(Ordered.LOWEST_PRECEDENCE) // La prioridad más baja para que sea el último recurso
public class DefaultExceptionHandler implements ExceptionHandlerStrategy {

    public DefaultExceptionHandler() {
        // El constructor está vacío intencionalmente.
    }

    @Override
    public boolean supports(Class<? extends Throwable> type) {
        return true; // Atrapa todos los errores que los otros no atraparon
    }

    @Override
    public Mono<ErrorResponseWrapper> handle(Throwable ex, ServerWebExchange exchange) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        String responseMessage = "Ocurrió un error inesperado. Detalle: " + ex.getMessage();

        ErrorBody body = new ErrorBody(status.value(), "Internal Server Error", responseMessage, null);
        return Mono.just(new ErrorResponseWrapper(status, body));
    }
}
