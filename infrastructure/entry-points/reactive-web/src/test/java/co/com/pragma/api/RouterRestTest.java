package co.com.pragma.api;

import co.com.pragma.api.dto.request.ApplicationRequestRecord;
import co.com.pragma.api.dto.response.ResponseRecord;
import co.com.pragma.api.mapper.IApplicationMapper;
import co.com.pragma.model.application.Application;
import co.com.pragma.model.application.ApplicationCreationResult;
import co.com.pragma.model.loantype.LoanType;
import co.com.pragma.model.status.Status;
import co.com.pragma.model.user.UserRecord;
import co.com.pragma.usecase.application.ApplicationUseCase;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.UUID;

@ExtendWith(SpringExtension.class)
@WebFluxTest
@Import({RouterRest.class, Handler.class, RouterRestTest.TestConfig.class})
class RouterRestTest {

    @Autowired
    private WebTestClient webTestClient;
    @Autowired
    private ApplicationUseCase useCase;
    @Autowired
    private IApplicationMapper mapper;
    @Autowired
    private Validator validator;

    @Test
    void shouldCreateLoanApplicationSuccessfully() {
        var requestRecord = new ApplicationRequestRecord(
                UUID.randomUUID(),
                BigDecimal.TEN,
                12,
                "test@test.com",
                1,
                1
        );

        var mockUseCaseResponse = new ApplicationCreationResult(
                new Application(UUID.randomUUID(), "", BigDecimal.TEN, 12, "test@test.com", 1, 1),
                new LoanType(1, "LIBRE INVERSION", BigDecimal.ONE, BigDecimal.TEN, BigDecimal.valueOf(0.05), true),
                new Status(1, "PENDIENTE", "Pendiente de revisión"),
                new UserRecord("1", "Nombre", "Apellido", LocalDate.of(1990, 1, 1), "test@test.com", "123456", "3001234567", 2, 50000.0)
        );

        Mockito.when(validator.validate(Mockito.any(ApplicationRequestRecord.class)))
                .thenReturn(Collections.emptySet());

        Mockito.when(mapper.toModel(Mockito.any(ApplicationRequestRecord.class)))
                .thenReturn(new Application(UUID.randomUUID(), "", BigDecimal.TEN, 12, "test@test.com", 1, 1));

        Mockito.when(useCase.createLoanApplication(Mockito.any(Application.class)))
                .thenReturn(Mono.just(mockUseCaseResponse));

        Mockito.when(mapper.toResponse(Mockito.any(ApplicationCreationResult.class)))
                .thenReturn(new ResponseRecord(null, null, null));

        webTestClient.post()
                .uri("/api/v1/solicitud")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestRecord)
                .exchange()
                .expectStatus().isCreated();
    }

    // Esta clase vacía actúa como el ancla que Spring Boot necesita para iniciar el contexto de prueba.
    @SpringBootConfiguration
    static class TestApplication {
    }

    /**
     * Configuración de prueba anidada.
     * Le dice a este test cómo crear los beans que el Handler necesita,
     * usando mocks que podemos controlar.
     */
    @TestConfiguration
    static class TestConfig {
        @Bean
        public ApplicationUseCase applicationUseCase() {
            return Mockito.mock(ApplicationUseCase.class);
        }

        @Bean
        public IApplicationMapper iApplicationMapper() {
            return Mockito.mock(IApplicationMapper.class);
        }

        @Bean
        public Validator validator() {
            return Mockito.mock(Validator.class);
        }
    }
}