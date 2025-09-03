package co.com.pragma.api.exception;

import co.com.pragma.api.exception.strategy.ExceptionHandlerStrategy;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Component
@Order(-2) // Se asegura de que se ejecute antes que el manejador de errores por defecto de Spring
@RequiredArgsConstructor
public class GlobalExceptionHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper;
    private final List<ExceptionHandlerStrategy> strategies;

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        // Encuentra la primera estrategia que soporta el tipo de excepción
        return strategies.stream()
                .filter(strategy -> strategy.supports(ex.getClass()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No se encontró un manejador de excepciones por defecto."))
                .handle(ex, exchange)
                .flatMap(errorWrapper -> {
                    exchange.getResponse().setStatusCode(errorWrapper.status());
                    exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
                    try {
                        byte[] bytes = objectMapper.writeValueAsBytes(errorWrapper.body());
                        return exchange.getResponse().writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(bytes)));
                    } catch (JsonProcessingException e) {
                        log.error("Error escribiendo la respuesta de error en formato JSON", e);
                        return Mono.error(e);
                    }
                });
    }
}