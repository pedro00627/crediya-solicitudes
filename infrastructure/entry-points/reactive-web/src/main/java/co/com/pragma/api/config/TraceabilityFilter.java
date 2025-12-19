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
@Order(1) // Se ejecuta después del filtro JWT para no interferir con el contexto de autenticación.
public class TraceabilityFilter implements WebFilter {

    public static final String CORRELATION_ID_KEY = "correlationId";
    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";

    @Override
    public Mono<Void> filter(final ServerWebExchange exchange, final WebFilterChain chain) {
        // Se usa Optional para obtener el encabezado y se genera un UUID si no está presente.
        // Esto asegura que 'correlationId' sea efectivamente final.
        final String correlationId = Optional.ofNullable(exchange.getRequest().getHeaders().getFirst(TraceabilityFilter.CORRELATION_ID_HEADER))
                .orElse(UUID.randomUUID().toString());

        // Añade el ID de correlación al Contexto de Reactor preservando el contexto existente
        return chain.filter(exchange)
                .contextWrite(ctx -> {
                    // Preservar todo el contexto existente y solo añadir/actualizar el correlationId
                    return ctx.put(TraceabilityFilter.CORRELATION_ID_KEY, correlationId);
                });
    }
}
