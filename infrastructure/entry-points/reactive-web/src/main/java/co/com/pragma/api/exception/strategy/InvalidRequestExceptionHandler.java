package co.com.pragma.api.exception.strategy;

import co.com.pragma.api.exception.InvalidRequestException;
import co.com.pragma.api.exception.dto.ErrorBody;
import co.com.pragma.api.exception.dto.ErrorResponseWrapper;
import jakarta.validation.ConstraintViolation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@Order(0) // Prioridad más alta para excepciones específicas
public class InvalidRequestExceptionHandler implements ExceptionHandlerStrategy {

    @Override
    public boolean supports(Class<? extends Throwable> type) {
        return InvalidRequestException.class.isAssignableFrom(type);
    }

    @Override
    public Mono<ErrorResponseWrapper> handle(Throwable ex, ServerWebExchange exchange) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        InvalidRequestException exception = (InvalidRequestException) ex;

        // Se añade una función de merge para manejar múltiples errores en un mismo campo
        Map<String, String> messages = exception.getViolations().stream()
                .collect(Collectors.toMap(
                        v -> v.getPropertyPath().toString(),
                        ConstraintViolation::getMessage,
                        (existingMsg, newMsg) -> existingMsg + "; " + newMsg
                ));

        log.warn("Error de validación en la petición [{}]: {}", exchange.getRequest().getPath(), messages);

        ErrorBody body = new ErrorBody(status.value(), "Validation Error", null, messages);
        return Mono.just(new ErrorResponseWrapper(status, body));
    }
}