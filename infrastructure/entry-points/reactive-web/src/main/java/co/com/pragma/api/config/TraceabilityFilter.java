package co.com.pragma.api.config;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.UUID;

/**
 * Filtro web que intercepta cada petición para asegurar que exista un ID de correlación.
 * Este ID se añade al Contexto de Reactor para que esté disponible en toda la cadena
 * reactiva, permitiendo una trazabilidad completa en los logs.
 */
@Component
@Order(-1) // Se ejecuta antes que otros filtros para asegurar que el contexto esté disponible.
public class TraceabilityFilter implements WebFilter {

    public static final String CORRELATION_ID_KEY = "correlationId";
    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        // Se usa Optional para obtener el encabezado y se genera un UUID si no está presente.
        // Esto asegura que 'correlationId' sea efectivamente final.
        String correlationId = Optional.ofNullable(exchange.getRequest().getHeaders().getFirst(CORRELATION_ID_HEADER))
                .orElse(UUID.randomUUID().toString());

        // Añade el ID de correlación al Contexto de Reactor y continúa la cadena de filtros.
        return chain.filter(exchange)
                .contextWrite(ctx -> ctx.put(CORRELATION_ID_KEY, correlationId));
    }
}
