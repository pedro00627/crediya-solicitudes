package co.com.pragma.consumer;


import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

import java.io.IOException;

class RestConsumerTest {

    public static MockWebServer mockBackEnd;
    private static RestConsumer restConsumer;

    @BeforeAll
    static void setUp() throws IOException {
        mockBackEnd = new MockWebServer();
        mockBackEnd.start();

        // CORREGIDO: Se obtienen todas las dependencias ANTES de crear el objeto.
        String baseUrl = mockBackEnd.url("/").toString();
        WebClient.Builder webClientBuilder = WebClient.builder();

        // CORREGIDO: Se pasan TODAS las dependencias requeridas al constructor.
        restConsumer = new RestConsumer(webClientBuilder, baseUrl);
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockBackEnd.shutdown();
    }

    @Test
    @DisplayName("Debe encontrar un usuario por email exitosamente")
    void shouldFindUserByEmail() {
        // Arrange: Preparamos la respuesta que el servidor mock debe devolver
        String email = "test@pragma.com.co";
        String jsonResponse = "{\"id\": \"1\", \"firstName\": \"Nombre\", \"lastName\": \"Apellido\", \"birthDate\": \"1990-01-01\", \"email\": \"" + email + "\", \"identityDocument\": \"123456\", \"phone\": \"3001234567\", \"roleId\": 2, \"baseSalary\": 50000.0}";

        mockBackEnd.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setResponseCode(HttpStatus.OK.value())
                .setBody(jsonResponse));

        // Act: Llamamos al método que queremos probar
        var response = restConsumer.findUserByEmail(email);

        // Assert: Verificamos que la respuesta es la esperada
        StepVerifier.create(response)
                .expectNextMatches(user -> user.getEmail().equals(email) && user.getRoleId().equals(2))
                .verifyComplete();
    }
}