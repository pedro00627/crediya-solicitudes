package co.com.pragma.consumer;

import co.com.pragma.api.security.JWTAuthenticationFilter;
import co.com.pragma.model.log.gateways.LoggerPort;
import co.com.pragma.model.user.UserRecord;
import co.com.pragma.model.user.gateways.UserGateway;
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

    public RestConsumer(WebClient.Builder webClientBuilder, @Value("${adapters.user-api.url}") String apiUrl, LoggerPort logger) {
        this.webClientBuilder = webClientBuilder;
        this.apiUrl = apiUrl;
        this.logger = logger;
    }

    @Override
    @CircuitBreaker(name = "user-api")
    public Mono<UserRecord> findUserByEmail(String email) {
        logger.info("Consultando servicio de usuarios por email: {}", logger.maskEmail(email));
        return Mono.deferContextual(contextView -> {
            String authToken = contextView.getOrDefault(JWTAuthenticationFilter.AUTH_TOKEN_KEY, "");
            return webClientBuilder.baseUrl(apiUrl).build()
                    .get()
                    .uri(uriBuilder -> uriBuilder.path("/api/v1/usuarios")
                            .queryParam("email", email)
                            .build())
                    .header("Authorization", "Bearer " + authToken) // Usar el token del contexto
                    .retrieve()
                    .bodyToMono(UserRecord.class)
                    .doOnError(error -> {
                        String errorMessage = String.format("Error al consultar el servicio de usuarios por email %s", logger.maskEmail(email));
                        logger.error(errorMessage, error);
                    })
                    .onErrorResume(WebClientResponseException.NotFound.class, e -> Mono.empty());
        });
    }
}
