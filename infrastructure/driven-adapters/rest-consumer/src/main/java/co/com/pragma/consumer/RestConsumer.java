package co.com.pragma.consumer;

import co.com.pragma.model.user.UserRecord;
import co.com.pragma.model.user.gateways.UserGateway;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class RestConsumer implements UserGateway {

    private final WebClient.Builder webClientBuilder;
    private final String apiUrl;

    // Inyectamos el valor de la propiedad directamente en el constructor
    public RestConsumer(WebClient.Builder webClientBuilder, @Value("${adapter.user-api.url}") String apiUrl) {
        this.webClientBuilder = webClientBuilder;
        this.apiUrl = apiUrl;
    }

    @Override
    @CircuitBreaker(name = "user-api")
    public Mono<UserRecord> findUserByEmail(String email) {
        return webClientBuilder.baseUrl(apiUrl).build()
                .get()
                .uri(uriBuilder -> uriBuilder.path("/api/v1/usuarios").queryParam("email", email).build())
                .retrieve()
                .bodyToMono(UserRecord.class);
    }
}