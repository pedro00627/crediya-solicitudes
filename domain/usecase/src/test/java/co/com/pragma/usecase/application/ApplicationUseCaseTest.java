package co.com.pragma.usecase.application;

import co.com.pragma.model.application.Application;
import co.com.pragma.model.application.ApplicationCreationResult;
import co.com.pragma.model.application.gateways.ApplicationGateway;
import co.com.pragma.model.config.AppRules;
import co.com.pragma.model.exception.BusinessException;
import co.com.pragma.model.exception.BusinessMessages;
import co.com.pragma.model.loantype.LoanType;
import co.com.pragma.model.loantype.gateways.LoanTypeGateway;
import co.com.pragma.model.log.gateways.LoggerPort;
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
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT) // Añadido para hacer todos los stubbings lenient
class ApplicationUseCaseTest {

    @Mock
    private LoanTypeGateway loanTypeGateway;
    @Mock
    private StatusGateway statusGateway;
    @Mock
    private UserGateway userGateway;
    @Mock
    private ApplicationGateway applicationGateway;
    @Mock
    private LoggerPort loggerPort;
    @Mock

    // La unidad bajo prueba ya no se inyecta, se construye manualmente.
    private CreateLoanApplicationUseCase applicationUseCase;

    private Application applicationRequest;
    private LoanType validLoanType;
    private UserRecord validUser;

    private Status validStatus;

    @BeforeEach
    void setUp() {
        // --- Valores de Reglas de Negocio (Hardcoded para la prueba) ---
        final AppRules appRules = new AppRules(2, 1, List.of(2, 3, 4));

        // --- Se instancia la clase bajo prueba manualmente ---
        this.applicationUseCase = new ApplicationUseCase(
                this.loanTypeGateway,
                this.statusGateway,
                this.userGateway,
                this.applicationGateway,
                this.loggerPort,
                appRules
        );

        // --- Objetos de Dominio Válidos ---
        this.applicationRequest = new Application(null, "", BigDecimal.valueOf(10000), 12, "test@test.com", 1, 1);
        this.validLoanType = new LoanType(1, "LIBRE INVERSION", BigDecimal.valueOf(5000), BigDecimal.valueOf(20000), BigDecimal.valueOf(0.05), true);
        this.validUser = new UserRecord("1", "Test", "Client", LocalDate.now(), "test@test.com", "123", "300", 2, 50000.0);
        this.validStatus = new Status(1, "PENDIENTE", "Pendiente de revisión");

        // Los mocks se configuran ahora en cada prueba individual para mayor claridad y evitar UnnecessaryStubbingException.
    }

    @Test
    @DisplayName("AC8: Debe crear una solicitud exitosamente cuando todas las validaciones pasan")
    void shouldCreateApplicationSuccessfullyWhenAllValidationsPass() {
        // Arrange
        when(this.loanTypeGateway.findById(anyInt())).thenReturn(Mono.just(this.validLoanType));
        when(this.userGateway.findUserByEmail(anyString())).thenReturn(Mono.just(this.validUser));
        when(this.statusGateway.findById(anyInt())).thenReturn(Mono.just(this.validStatus));
        when(this.applicationGateway.findOpenApplicationsByDocumentId(anyString(), any())).thenReturn(Flux.empty());
        when(this.applicationGateway.save(any(Application.class))).thenReturn(Mono.just(this.applicationRequest));

        // Act
        final Mono<ApplicationCreationResult> result = this.applicationUseCase.createLoanApplication(this.applicationRequest);

        // Assert
        StepVerifier.create(result)
                .assertNext(creationResult -> {
                    assertNotNull(creationResult);
                    assertEquals(this.validUser.getEmail(), creationResult.user().getEmail());
                    assertEquals(this.validLoanType.getName(), creationResult.loanType().getName());
                    assertEquals(this.validStatus.getName(), creationResult.status().getName());
                })
                .verifyComplete();

        verify(this.applicationGateway).save(any(Application.class));
    }

    @Test
    @DisplayName("AC3: Debe lanzar BusinessException cuando el tipo de préstamo no existe")
    void shouldThrowBusinessExceptionWhenLoanTypeDoesNotExist() {
        // Arrange
        when(this.loanTypeGateway.findById(anyInt())).thenReturn(Mono.empty());
        // Aunque esperamos que el flujo falle antes, Mono.zip se suscribe a todas las fuentes.
        // Por lo tanto, debemos proporcionar mocks para las otras llamadas para evitar un NullPointerException.
        when(this.userGateway.findUserByEmail(anyString())).thenReturn(Mono.just(this.validUser));
        when(this.statusGateway.findById(anyInt())).thenReturn(Mono.just(this.validStatus));

        // Act
        final Mono<ApplicationCreationResult> result = this.applicationUseCase.createLoanApplication(this.applicationRequest);

        // Assert
        StepVerifier.create(result).expectError(BusinessException.class).verify();
    }

    @Test
    @DisplayName("AC5: Debe lanzar BusinessException cuando el usuario no existe")
    void shouldThrowBusinessExceptionWhenUserDoesNotExist() {
        // Arrange
        // Para que el flujo llegue a la validación del usuario, las validaciones anteriores deben pasar.
        when(this.loanTypeGateway.findById(anyInt())).thenReturn(Mono.just(this.validLoanType));
        when(this.statusGateway.findById(anyInt())).thenReturn(Mono.just(this.validStatus));
        // Configuramos el mock del usuario para que falle.
        when(this.userGateway.findUserByEmail(anyString())).thenReturn(Mono.empty());

        // Act
        final Mono<ApplicationCreationResult> result = this.applicationUseCase.createLoanApplication(this.applicationRequest);

        // Assert
        StepVerifier.create(result).expectError(BusinessException.class).verify();
    }

    @Test
    @DisplayName("AC6: Debe lanzar BusinessException cuando el rol del usuario no es Cliente")
    void shouldThrowBusinessExceptionWhenUserRoleIsNotClient() {
        // Arrange
        // Para que el flujo llegue a la validación del rol, las validaciones anteriores deben pasar.
        when(this.loanTypeGateway.findById(anyInt())).thenReturn(Mono.just(this.validLoanType));
        when(this.statusGateway.findById(anyInt())).thenReturn(Mono.just(this.validStatus));
        final UserRecord userWithWrongRole = new UserRecord("1", "Test", "User", LocalDate.now(), "test@test.com", "123", "300", 1, 50000.0);
        when(this.userGateway.findUserByEmail(anyString())).thenReturn(Mono.just(userWithWrongRole));
        // Se añade el mock para que el flujo pase la validación de solicitudes abiertas.
        when(this.applicationGateway.findOpenApplicationsByDocumentId(anyString(), any())).thenReturn(Flux.empty());

        // Act
        final Mono<ApplicationCreationResult> result = this.applicationUseCase.createLoanApplication(this.applicationRequest);

        // Assert
        StepVerifier.create(result).expectError(BusinessException.class).verify();
    }

    @Test
    @DisplayName("AC4: Debe lanzar BusinessException cuando el monto está fuera de rango")
    void shouldThrowBusinessExceptionWhenAmountIsOutOfRange() {
        // Arrange
        // Para que el flujo llegue a la validación del monto, las validaciones anteriores deben pasar.
        when(this.loanTypeGateway.findById(anyInt())).thenReturn(Mono.just(this.validLoanType));
        when(this.userGateway.findUserByEmail(anyString())).thenReturn(Mono.just(this.validUser));
        when(this.statusGateway.findById(anyInt())).thenReturn(Mono.just(this.validStatus));
        when(this.applicationGateway.findOpenApplicationsByDocumentId(anyString(), any())).thenReturn(Flux.empty());
        final Application requestWithInvalidAmount = new Application(null, "", BigDecimal.valueOf(4000), 12, "test@test.com", 1, 1);

        // Act
        final Mono<ApplicationCreationResult> result = this.applicationUseCase.createLoanApplication(requestWithInvalidAmount);

        // Assert
        StepVerifier.create(result).expectError(BusinessException.class).verify();
    }

    @Test
    @DisplayName("AC7: Debe lanzar BusinessException si el cliente ya tiene una solicitud abierta")
    void shouldThrowBusinessExceptionWhenOpenApplicationExists() {
        // Arrange
        // Para que el flujo llegue a la validación de solicitudes abiertas, las validaciones anteriores deben pasar.
        when(this.loanTypeGateway.findById(anyInt())).thenReturn(Mono.just(this.validLoanType));
        when(this.userGateway.findUserByEmail(anyString())).thenReturn(Mono.just(this.validUser));
        when(this.statusGateway.findById(anyInt())).thenReturn(Mono.just(this.validStatus));

        // Simulamos que el repositorio encuentra una solicitud abierta para este cliente.
        when(this.applicationGateway.findOpenApplicationsByDocumentId(anyString(), any()))
                .thenReturn(Flux.just(new Application(UUID.randomUUID(), "doc123", BigDecimal.ONE, 1, "email@test.com", 1, 1)));

        // Act
        final Mono<ApplicationCreationResult> result = this.applicationUseCase.createLoanApplication(this.applicationRequest);

        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof BusinessException &&
                        throwable.getMessage().equals(BusinessMessages.USER_HAS_ACTIVE_APPLICATION))
                .verify();
    }
}
