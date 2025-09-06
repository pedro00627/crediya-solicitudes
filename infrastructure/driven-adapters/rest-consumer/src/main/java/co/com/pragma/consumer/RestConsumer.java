package co.com.pragma.consumer;

import co.com.pragma.commonutils.LogHelper;
import co.com.pragma.model.user.UserRecord;
import co.com.pragma.model.user.gateways.UserGateway;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class RestConsumer implements UserGateway {

    private final WebClient.Builder webClientBuilder;
    private final String apiUrl;

    // Se inyecta el valor de la propiedad desde la ruta unificada y correcta.
    public RestConsumer(WebClient.Builder webClientBuilder, @Value("${adapters.user-api.url}") String apiUrl) {
        this.webClientBuilder = webClientBuilder;
        this.apiUrl = apiUrl;
    }

    @Override
    @CircuitBreaker(name = "user-api")
    public Mono<UserRecord> findUserByEmail(String email) {
        log.info("Consultando servicio de usuarios por email: {}", LogHelper.maskEmail(email));
        return webClientBuilder.baseUrl(apiUrl).build()
                .get()
                .uri(uriBuilder -> uriBuilder.path("/api/v1/usuarios").queryParam("email", email).build())
                .retrieve()
                .bodyToMono(UserRecord.class)
                .doOnError(error -> log.error("Error al consultar el servicio de usuarios por email {}: {}",
                        LogHelper.maskEmail(email), error.getMessage()))
                // Se añade el manejo de error para el 404.
                // Si se recibe un NotFound, se traduce a un Mono vacío, cumpliendo el contrato del Gateway.
                .onErrorResume(WebClientResponseException.NotFound.class, e -> Mono.empty());
    }
}