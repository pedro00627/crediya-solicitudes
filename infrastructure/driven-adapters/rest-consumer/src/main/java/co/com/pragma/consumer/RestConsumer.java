package co.com.pragma.consumer;

import co.com.pragma.model.log.gateways.LoggerPort;
import co.com.pragma.model.user.UserRecord;
import co.com.pragma.model.user.gateways.UserGateway;
import co.com.pragma.security.api.JWTAuthenticationFilter;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@Service
public class RestConsumer implements UserGateway {

    private final WebClient.Builder webClientBuilder;
    private final String apiUrl;
    private final LoggerPort logger;

    public RestConsumer(final WebClient.Builder webClientBuilder, @Value("${adapters.user-api.url}") final String apiUrl, final LoggerPort logger) {
        this.webClientBuilder = webClientBuilder;
        this.apiUrl = apiUrl;
        this.logger = logger;
    }

    @Override
    @CircuitBreaker(name = "user-api")
    public Mono<UserRecord> findUserByEmail(final String email) {
        this.logger.info("Consultando servicio de usuarios por email: {}", this.logger.maskEmail(email));
        return Mono.deferContextual(contextView -> {
            final String authToken = contextView.getOrDefault(JWTAuthenticationFilter.AUTH_TOKEN_KEY, "");
            return this.webClientBuilder.baseUrl(this.apiUrl).build()
                    .get()
                    .uri(uriBuilder -> uriBuilder.path("/api/v1/usuarios")
                            .queryParam("email", email)
                            .build())
                    .header("Authorization", authToken) // Usar el token del contexto
                    .retrieve()
                    .bodyToMono(UserRecord.class)
                    .doOnError(error -> {
                        final String errorMessage = String.format("Error al consultar el servicio de usuarios por email %s", this.logger.maskEmail(email));
                        this.logger.error(errorMessage, error);
                    })
                    .onErrorResume(WebClientResponseException.NotFound.class, e -> Mono.empty());
        });
    }
}
