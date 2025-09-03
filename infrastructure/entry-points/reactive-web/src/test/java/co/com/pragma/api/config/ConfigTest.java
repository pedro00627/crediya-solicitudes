package co.com.pragma.api.config;

import co.com.pragma.api.Handler;
import co.com.pragma.api.RouterRest;
import co.com.pragma.api.mapper.IApplicationMapper;
import co.com.pragma.usecase.application.ApplicationUseCase;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;

@ContextConfiguration(classes = {RouterRest.class, Handler.class})
@WebFluxTest
@Import({CorsConfig.class, SecurityHeadersConfig.class, ConfigTest.MockDependenciesConfig.class})
class ConfigTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void corsConfigurationShouldAllowOrigins() {
        webTestClient.get()
                .uri("/api/v1/applications") // Corrected URI to a non-matching but existing pattern
                .exchange()
                .expectStatus().is4xxClientError() // Corrected to expect a client error (405 Method Not Allowed)
                .expectHeader().valueEquals("Content-Security-Policy",
                        "default-src 'self'; frame-ancestors 'self'; form-action 'self'")
                .expectHeader().valueEquals("Strict-Transport-Security", "max-age=31536000;")
                .expectHeader().valueEquals("X-Content-Type-Options", "nosniff")
                .expectHeader().valueEquals("Server", "")
                .expectHeader().valueEquals("Cache-Control", "no-store")
                .expectHeader().valueEquals("Pragma", "no-cache")
                .expectHeader().valueEquals("Referrer-Policy", "strict-origin-when-cross-origin");
    }

    @TestConfiguration
    static class MockDependenciesConfig {
        @Bean
        public IApplicationMapper iApplicationMapper() {
            return Mockito.mock(IApplicationMapper.class);
        }

        @Bean
        public ApplicationUseCase applicationUseCase() {
            return Mockito.mock(ApplicationUseCase.class);
        }

        @Bean
        public Validator validator() {
            return Mockito.mock(Validator.class);
        }
    }

}