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
import org.springframework.web.util.UriComponentsBuilder;
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
        this.logger.info("=== REST CONSUMER INICIADO ===");
        this.logger.info("Consultando servicio de usuarios por email: {}", this.logger.maskEmail(email));
        this.logger.info("API URL base: {}", this.apiUrl);

        return Mono.deferContextual(contextView -> {
            final String authToken = contextView.getOrDefault(JWTAuthenticationFilter.AUTH_TOKEN_KEY, "");
            this.logger.info("Contexto reactivo completo: {}", contextView);
            this.logger.info("Token key buscado: {}", JWTAuthenticationFilter.AUTH_TOKEN_KEY);
            this.logger.info("Token extraído del contexto: [{}]", authToken != null ? authToken.substring(0, Math.min(authToken.length(), 20)) + "..." : "null");
            this.logger.info("Token length: {}", authToken != null ? authToken.length() : "null");

            // Usar UriComponentsBuilder para codificación correcta
            String uriString = UriComponentsBuilder
                    .fromHttpUrl(this.apiUrl + "/api/v1/usuarios")
                    .queryParam("email", email)
                    .encode()
                    .toUriString();

            this.logger.info("URL construida con UriComponentsBuilder: {}", uriString);
            this.logger.info("Email original: [{}]", email);
            this.logger.info("Enviando header Authorization: [{}]", authToken != null && !authToken.isEmpty() ? authToken.substring(0, Math.min(authToken.length(), 20)) + "..." : "EMPTY/NULL");

            return this.webClientBuilder.build()
                    .get()
                    .uri(uriString)
                    .header("Authorization", authToken)
                    .retrieve()
                    .bodyToMono(UserRecord.class)
                    .doOnSuccess(user -> {
                        if (user != null) {
                            this.logger.info("Usuario encontrado exitosamente: {}", this.logger.maskEmail(user.getEmail()));
                        } else {
                            this.logger.info("Respuesta exitosa pero usuario no encontrado (404)");
                        }
                    })
                    .doOnError(error -> {
                        final String errorMessage = String.format("Error al consultar el servicio de usuarios por email %s: %s",
                            this.logger.maskEmail(email), error.getMessage());
                        this.logger.error(errorMessage, error);
                    })
                    .onErrorResume(WebClientResponseException.NotFound.class, e -> {
                        this.logger.info("Usuario no encontrado (404) para email: {}", this.logger.maskEmail(email));
                        return Mono.empty();
                    });
        });
    }
}
