package co.com.pragma.api;

import co.com.pragma.api.dto.request.ApplicationRequestRecord;
import co.com.pragma.api.mapper.ApplicationRequestMapper;
import co.com.pragma.model.application.ApplicationReviewDTO;
import co.com.pragma.api.dto.response.ApplicationResponseRecord;
import co.com.pragma.api.dto.response.LoanTypeResponseRecord;
import co.com.pragma.api.dto.response.ResponseRecord;
import co.com.pragma.api.dto.response.StatusResponseRecord;
import co.com.pragma.api.mapper.IApplicationMapper;
import co.com.pragma.api.mapper.ApplicationMapperAdapter;
import co.com.pragma.model.application.Application;
import co.com.pragma.model.application.ApplicationCreationResult;
import co.com.pragma.model.loantype.LoanType;
import co.com.pragma.model.loantype.gateways.LoanTypeGateway;
import co.com.pragma.model.log.gateways.LoggerPort;
import co.com.pragma.model.status.Status;
import co.com.pragma.model.status.gateways.StatusGateway;
import co.com.pragma.model.user.UserRecord;
import co.com.pragma.model.application.gateways.FindApplicationsForReviewUseCase;
import co.com.pragma.usecase.application.ApplicationUseCase;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.UUID;

@ExtendWith(SpringExtension.class)
@WebFluxTest(excludeAutoConfiguration = {
        SecurityAutoConfiguration.class,
        ReactiveSecurityAutoConfiguration.class
})
// Importar RouterRest y Handler explícitamente para asegurar que las rutas se registren
@Import({RouterRest.class, ApplicationCommandHandler.class, ApplicationQueryHandler.class, ApplicationRequestMapper.class, RouterRestTest.TestApplication.class})class RouterRestTest {

    private final WebTestClient webTestClient;

    @MockitoBean
    private ApplicationUseCase useCase;

    @MockitoBean
    private FindApplicationsForReviewUseCase findApplicationsForReviewUseCase;

    @MockitoBean
    private ApplicationMapperAdapter mapperAdapter;

    @MockitoBean
    private ApplicationRequestMapper requestMapper;

    @MockitoBean
    private IApplicationMapper mapper;

    @MockitoBean
    private Validator validator;

    @MockitoBean
    private LoggerPort loggerPort;

    @MockitoBean
    private StatusGateway statusGateway;

    @MockitoBean
    private LoanTypeGateway loanTypeGateway;

    public RouterRestTest(@Autowired WebTestClient webTestClient) {
        this.webTestClient = webTestClient;
    }

    @Test
    void shouldCreateLoanApplicationSuccessfully() {
        var requestRecord = new ApplicationRequestRecord(
                UUID.randomUUID(),
                BigDecimal.TEN,
                12,
                "test@test.com",
                "PENDIENTE",
                "LIBRE INVERSION"
        );

        var mockApplication = new Application(UUID.randomUUID(), "", BigDecimal.TEN, 12, "test@test.com", 1, 1);
        var mockLoanType = new LoanType(1, "LIBRE INVERSION", BigDecimal.ONE, BigDecimal.TEN, BigDecimal.valueOf(0.05), true);
        var mockStatus = new Status(1, "PENDIENTE", "Pendiente de revisión");
        var mockUser = new UserRecord("1", "Nombre", "Apellido", LocalDate.of(1990, 1, 1), "test@test.com", "123456", "3001234567", 2, 50000.0);

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
                .thenReturn(Mono.just(mockUseCaseResponse));

        Mockito.when(mapper.toResponse(Mockito.any(ApplicationCreationResult.class)))
                .thenReturn(new ResponseRecord(
                        new ApplicationResponseRecord(mockApplication.getApplicationId(), mockApplication.getAmount(), mockApplication.getTerm(), mockApplication.getEmail(), mockApplication.getStatusId(), mockApplication.getLoanTypeId()),
                        new StatusResponseRecord(mockStatus.getStatusId(), mockStatus.getName(), mockStatus.getDescription()),
                        new LoanTypeResponseRecord(mockLoanType.getLoanTypeId(), mockLoanType.getName(), mockLoanType.getMinAmount(), mockLoanType.getMaxAmount(), mockLoanType.getInterestRate(), mockLoanType.isAutoValidation())
                ));

        webTestClient
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

        Mockito.when(mapperAdapter.toEnrichedReviewDTO(Mockito.any(Application.class)))
                .thenReturn(Mono.just(mockReviewDTO));

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
    }
}