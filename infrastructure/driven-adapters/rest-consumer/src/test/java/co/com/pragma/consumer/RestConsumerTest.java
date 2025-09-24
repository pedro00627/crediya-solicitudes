package co.com.pragma.consumer;

import co.com.pragma.model.log.gateways.LoggerPort;
import co.com.pragma.model.user.UserRecord;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class RestConsumerTest {

    private static final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
    public static MockWebServer mockBackEnd;
    private static RestConsumer restConsumer;

    @BeforeAll
    static void setUp() throws IOException {
        RestConsumerTest.mockBackEnd = new MockWebServer();
        RestConsumerTest.mockBackEnd.start();

        final String baseUrl = RestConsumerTest.mockBackEnd.url("/").toString();
        final WebClient.Builder webClientBuilder = WebClient.builder();

        final LoggerPort logger = Mockito.mock(LoggerPort.class); // Initialize the logger mock
        RestConsumerTest.restConsumer = new RestConsumer(webClientBuilder, baseUrl, logger);
    }

    @AfterAll
    static void tearDown() throws IOException {
        RestConsumerTest.mockBackEnd.shutdown();
    }

    @Test
    @DisplayName("Debe encontrar un usuario por email exitosamente")
    void shouldFindUserByEmail() throws JsonProcessingException {
        // Arrange: Preparamos la respuesta que el servidor mock debe devolver
        final String email = "test@pragma.com.co";
        final UserRecord mockUser = new UserRecord(
                "1",
                "Nombre",
                "Apellido",
                LocalDate.of(1990, 1, 1),
                email,
                "123456",
                "3001234567",
                2,
                50000.0
        );
        // Se crea el JSON a partir del objeto real, haciendo el test más robusto.
        final String jsonResponse = RestConsumerTest.objectMapper.writeValueAsString(mockUser);

        RestConsumerTest.mockBackEnd.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setResponseCode(HttpStatus.OK.value())
                .setBody(jsonResponse));

        // Act: Llamamos al método que queremos probar
        final var response = RestConsumerTest.restConsumer.findUserByEmail(email);

        // Assert: Verificamos que la respuesta es la esperada
        StepVerifier.create(response)
                .expectNextMatches(user -> {
                    // MEJORA: Usar aserciones explícitas para mensajes de error más claros.
                    assertAll(
                            () -> assertEquals(email, user.getEmail(), "El email no coincide"),
                            () -> assertEquals(2, user.getRoleId(), "El ID del rol no coincide")
                    );
                    return true; // Si las aserciones pasan, la condición se cumple.
                })
                .verifyComplete();
    }
}