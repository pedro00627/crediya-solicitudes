package co.com.pragma.api;

import co.com.pragma.api.dto.ApplicationReviewDTO;
import co.com.pragma.api.dto.request.ApplicationRequestRecord;
import co.com.pragma.api.mapper.IApplicationRequestMapper;
import co.com.pragma.api.mapper.IApplicationResponseHandler;
import co.com.pragma.api.dto.response.ApplicationResponseRecord;
import co.com.pragma.api.dto.response.LoanTypeResponseRecord;
import co.com.pragma.api.dto.response.ResponseRecord;
import co.com.pragma.api.dto.response.StatusResponseRecord;
import co.com.pragma.api.mapper.ApplicationMapperAdapter;
import co.com.pragma.model.application.Application;
import co.com.pragma.model.application.ApplicationCreationResult;
import co.com.pragma.model.loantype.LoanType;
import co.com.pragma.model.log.gateways.LoggerPort;
import co.com.pragma.model.status.Status;
import co.com.pragma.model.user.UserRecord;
import co.com.pragma.usecase.application.CreateLoanApplicationUseCase;
import co.com.pragma.usecase.application.FindApplicationsForReviewUseCase;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@ExtendWith(SpringExtension.class)
@WebFluxTest(excludeAutoConfiguration = {
        SecurityAutoConfiguration.class
})
@Import({RouterRest.class, ApplicationCommandHandler.class, ApplicationQueryHandler.class, RouterRestTest.TestApplication.class})class RouterRestTest {

    private final WebTestClient webTestClient;
    @MockitoBean
    private CreateLoanApplicationUseCase useCase;
    @MockitoBean
    private FindApplicationsForReviewUseCase findApplicationsForReviewUseCase;
    @MockitoBean
    private IApplicationRequestMapper requestMapper;
    @MockitoBean
    private IApplicationResponseHandler responseHandler;
    @MockitoBean
    private ApplicationMapperAdapter mapperAdapter;
    @MockitoBean
    private Validator validator;
    @MockitoBean
    private LoggerPort loggerPort;

    public RouterRestTest(@Autowired WebTestClient webTestClient) {
        this.webTestClient = webTestClient;
    }

    @Test
    void shouldCreateLoanApplicationSuccessfully() {
        String userEmail = "test@test.com";

        var requestRecord = new ApplicationRequestRecord(
                null,
                BigDecimal.TEN,
                12,
                userEmail,
                null, // El statusName es nulo para que el caso de uso asigne PENDIENTE
                "LIBRE INVERSION"
        );

        var mockApplication = new Application(UUID.randomUUID(), "", BigDecimal.TEN, 12, userEmail, 1, 1);
        var mockLoanType = new LoanType(1, "LIBRE INVERSION", BigDecimal.ONE, BigDecimal.TEN, BigDecimal.valueOf(0.05), true);
        var mockStatus = new Status(1, "PENDIENTE", "Pendiente de revisión");
        var mockUser = new UserRecord("1", "Nombre", "Apellido", LocalDate.of(1990, 1, 1), userEmail, "123456", "3001234567", 2, 50000.0);

        var mockUseCaseResponse = new ApplicationCreationResult(
                mockApplication,
                mockLoanType,
                mockStatus,
                mockUser
        );

        Mockito.when(validator.validate(Mockito.any(ApplicationRequestRecord.class)))
                .thenReturn(Collections.emptySet());

        Mockito.when(requestMapper.toModel(Mockito.any(ApplicationRequestRecord.class)))
                .thenReturn(Mono.just(mockApplication));

        Mockito.when(useCase.createLoanApplication(Mockito.any(Application.class)))
                .thenAnswer(invocation -> Mono.just(mockUseCaseResponse));

        Mockito.when(responseHandler.buildCreationResponse(Mockito.any(ApplicationCreationResult.class)))
                .thenAnswer(invocation -> ServerResponse.created(URI.create("/api/v1/solicitud")).build());

        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockUser(userEmail))
                .post()
                .uri("/api/v1/solicitud")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestRecord)
                .exchange()
                .expectStatus().isCreated();
    }

    @Test
    void shouldGetApplicationsForReviewSuccessfully() {
        var mockApplication = new Application(UUID.randomUUID(), "12345", BigDecimal.valueOf(5000), 24, "review@test.com", 1, 1);
        var mockReviewDTO = ApplicationReviewDTO.builder()
                .email("review@test.com")
                .amount(BigDecimal.valueOf(5000))
                .applicationStatus("Pendiente de revisión")
                .loanType("LIBRE INVERSION")
                .build();

        Mockito.when(findApplicationsForReviewUseCase.countApplicationsForReview())
                .thenReturn(Mono.just(1L));
        Mockito.when(findApplicationsForReviewUseCase.findApplicationsForReview(Mockito.any()))
                .thenReturn(Flux.just(mockApplication));

        Mockito.when(mapperAdapter.toEnrichedReviewDTOs(Mockito.any())) // Changed from Mockito.any(Flux.class)
                .thenReturn(Mono.just(List.of(mockReviewDTO)));

        webTestClient
                .get()
                .uri("/api/v1/solicitud?page=0&size=1")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.totalItems").isEqualTo(1)
                .jsonPath("$.totalPages").isEqualTo(1)
                .jsonPath("$.currentPage").isEqualTo(0)
                .jsonPath("$.content[0].email").isEqualTo("review@test.com")
                .jsonPath("$.content[0].applicationStatus").isEqualTo("Pendiente de revisión");
    }

    @SpringBootConfiguration
    static class TestApplication {
        @Bean
        public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
            // Deshabilitar CSRF para las pruebas, que es la causa del 403 FORBIDDEN en los POST
            return http
                    .csrf(ServerHttpSecurity.CsrfSpec::disable)
                    .authorizeExchange(exchange -> exchange.anyExchange().permitAll())
                    .build();
        }
    }
}
