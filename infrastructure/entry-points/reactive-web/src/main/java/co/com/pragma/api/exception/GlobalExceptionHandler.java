package co.com.pragma.api.exception;

import co.com.pragma.api.exception.strategy.ExceptionHandlerStrategy;
import co.com.pragma.model.log.gateways.LoggerPort;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@Order(-2) // Alta prioridad para interceptar errores antes que los manejadores por defecto de Spring
public class GlobalExceptionHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper;
    private final List<ExceptionHandlerStrategy> strategies;
    private final LoggerPort logger;

    public GlobalExceptionHandler(ObjectMapper objectMapper, List<ExceptionHandlerStrategy> strategies, LoggerPort logger) {
        this.objectMapper = objectMapper;
        this.strategies = strategies;
        this.logger = logger;
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        // Encuentra la primera estrategia que soporta este tipo de excepción
        return this.strategies.stream()
                .filter(strategy -> strategy.supports(ex.getClass()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No se encontró una estrategia de manejo de excepciones por defecto."))
                .handle(ex, exchange) // Delega el manejo a la estrategia encontrada
                .flatMap(errorWrapper -> {
                    // Loguea el error de forma centralizada y estructurada
                    if (errorWrapper.status().is5xxServerError()) {
                        String errorMessage = String.format("Error no controlado en la petición [%s %s]: %s",
                                exchange.getRequest().getMethod(),
                                exchange.getRequest().getPath(),
                                errorWrapper.body());
                        logger.error(errorMessage, ex); // Loguea el stack trace completo para errores del servidor
                    } else {
                        logger.warn("Error de negocio o de cliente en la petición [{} {}]: {}",
                                exchange.getRequest().getMethod(),
                                exchange.getRequest().getPath(),
                                errorWrapper.body()); // Para errores 4xx, el cuerpo del error suele ser suficiente
                    }

                    // Establece el código de estado y el tipo de contenido en la respuesta HTTP
                    exchange.getResponse().setStatusCode(errorWrapper.status());
                    exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

                    // Escribe el cuerpo del error en la respuesta
                    try {
                        byte[] bytes = objectMapper.writeValueAsBytes(errorWrapper.body());
                        return exchange.getResponse().writeWith(Mono.just(exchange.getResponse()
                                .bufferFactory().wrap(bytes)));
                    } catch (JsonProcessingException e) {
                        logger.error("Error escribiendo la respuesta JSON de error", e);
                        return Mono.empty(); // Se retorna Mono.empty() para no propagar un segundo error
                    }
                });
    }
}