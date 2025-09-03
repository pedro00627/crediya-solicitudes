package co.com.pragma.usecase.application;

import co.com.pragma.model.application.Application;
import co.com.pragma.model.application.ApplicationCreationResult;
import co.com.pragma.model.application.gateways.ApplicationRepository;
import co.com.pragma.model.exception.BusinessException;
import co.com.pragma.model.loantype.LoanType;
import co.com.pragma.model.loantype.gateways.LoanTypeGateway;
import co.com.pragma.model.status.Status;
import co.com.pragma.model.status.gateways.StatusGateway;
import co.com.pragma.model.user.UserRecord;
import co.com.pragma.model.user.gateways.UserGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApplicationUseCaseTest {

    @Mock
    private LoanTypeGateway loanTypeGateway;
    @Mock
    private StatusGateway statusGateway;
    @Mock
    private UserGateway userGateway;
    @Mock
    private ApplicationRepository applicationRepository;

    // La unidad bajo prueba ya no se inyecta, se construye manualmente.
    private ApplicationUseCase applicationUseCase;

    private Application applicationRequest;
    private LoanType validLoanType;
    private UserRecord validUser;
    private Status validStatus;

    @BeforeEach
    void setUp() {
        // --- Valores de Reglas de Negocio (Hardcoded para la prueba) ---
        Integer clientRoleId = 2;
        Integer pendingStatusId = 1;

        // --- Se instancia la clase bajo prueba manualmente ---
        applicationUseCase = new ApplicationUseCase(
                loanTypeGateway,
                statusGateway,
                userGateway,
                applicationRepository,
                clientRoleId,
                pendingStatusId
        );

        // --- Objetos de Dominio Válidos ---
        applicationRequest = new Application(null, BigDecimal.valueOf(10000), 12, "test@test.com", 1, 1);
        validLoanType = new LoanType(1, "LIBRE INVERSION", BigDecimal.valueOf(5000), BigDecimal.valueOf(20000), BigDecimal.valueOf(0.05), true);
        validUser = new UserRecord("1", "Test", "Client", LocalDate.now(), "test@test.com", "123", "300", 2, 50000.0);
        validStatus = new Status(1, "PENDIENTE", "Pendiente de revisión");

        // --- Mocks de "Camino Feliz" por Defecto ---
        when(loanTypeGateway.findById(anyInt())).thenReturn(Mono.just(validLoanType));
        when(userGateway.findUserByEmail(anyString())).thenReturn(Mono.just(validUser));
        when(statusGateway.findById(anyInt())).thenReturn(Mono.just(validStatus));
    }

    @Test
    @DisplayName("AC8: Debe crear una solicitud exitosamente cuando todas las validaciones pasan")
    void shouldCreateApplicationSuccessfullyWhenAllValidationsPass() {
        // Arrange
        when(applicationRepository.save(any(Application.class))).thenReturn(Mono.just(applicationRequest));

        // Act
        Mono<ApplicationCreationResult> result = applicationUseCase.createLoanApplication(applicationRequest);

        // Assert
        StepVerifier.create(result)
                .assertNext(creationResult -> {
                    assertNotNull(creationResult);
                    assertEquals(validUser.getEmail(), creationResult.getUser().getEmail());
                    assertEquals(validLoanType.getName(), creationResult.getLoanType().getName());
                    assertEquals(validStatus.getName(), creationResult.getStatus().getName());
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("AC3: Debe lanzar BusinessException cuando el tipo de préstamo no existe")
    void shouldThrowBusinessExceptionWhenLoanTypeDoesNotExist() {
        // Arrange
        when(loanTypeGateway.findById(anyInt())).thenReturn(Mono.empty());

        // Act
        Mono<ApplicationCreationResult> result = applicationUseCase.createLoanApplication(applicationRequest);

        // Assert
        StepVerifier.create(result).expectError(BusinessException.class).verify();
    }

    @Test
    @DisplayName("AC5: Debe lanzar BusinessException cuando el usuario no existe")
    void shouldThrowBusinessExceptionWhenUserDoesNotExist() {
        // Arrange
        when(userGateway.findUserByEmail(anyString())).thenReturn(Mono.empty());

        // Act
        Mono<ApplicationCreationResult> result = applicationUseCase.createLoanApplication(applicationRequest);

        // Assert
        StepVerifier.create(result).expectError(BusinessException.class).verify();
    }

    @Test
    @DisplayName("AC6: Debe lanzar BusinessException cuando el rol del usuario no es Cliente")
    void shouldThrowBusinessExceptionWhenUserRoleIsNotClient() {
        // Arrange
        UserRecord userWithWrongRole = new UserRecord("1", "Test", "User", LocalDate.now(), "test@test.com", "123", "300", 1, 50000.0);
        when(userGateway.findUserByEmail(anyString())).thenReturn(Mono.just(userWithWrongRole));

        // Act
        Mono<ApplicationCreationResult> result = applicationUseCase.createLoanApplication(applicationRequest);

        // Assert
        StepVerifier.create(result).expectError(BusinessException.class).verify();
    }

    @Test
    @DisplayName("AC4: Debe lanzar BusinessException cuando el monto está fuera de rango")
    void shouldThrowBusinessExceptionWhenAmountIsOutOfRange() {
        // Arrange
        Application requestWithInvalidAmount = new Application(null, BigDecimal.valueOf(4000), 12, "test@test.com", 1, 1);

        // Act
        Mono<ApplicationCreationResult> result = applicationUseCase.createLoanApplication(requestWithInvalidAmount);

        // Assert
        StepVerifier.create(result).expectError(BusinessException.class).verify();
    }
}
