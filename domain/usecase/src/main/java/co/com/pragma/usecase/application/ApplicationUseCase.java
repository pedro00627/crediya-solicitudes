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
import co.com.pragma.usecase.validator.ApplicationValidator;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class ApplicationUseCase {

    // Gateways
    private final LoanTypeGateway loanTypeGateway;
    private final StatusGateway statusGateway;
    private final UserGateway userGateway;
    private final ApplicationRepository applicationRepository;

    // Business rule values are now simple fields, injected via the constructor.
    private final Integer clientRoleId;
    private final Integer pendingStatusId;

    /**
     * Record interno para agrupar los datos necesarios para la validación.
     * Esto evita manejar Tuplas directamente y hace el código más legible.
     */
    private record ValidationData(LoanType loanType, UserRecord user, Status initialStatus) {}

    /**
     * Orquesta el proceso de creación de una nueva solicitud de préstamo.
     * El flujo es declarativo: obtener datos, validar, y luego guardar.
     */
    public Mono<ApplicationCreationResult> createLoanApplication(Application applicationRequest) {
        return fetchValidationData(applicationRequest)
                .flatMap(validationData -> validateBusinessRules(applicationRequest, validationData))
                .flatMap(validationData -> saveApplicationAndBuildResult(applicationRequest, validationData));
    }

    /**
     * Paso 1: Obtiene todos los datos externos necesarios de forma concurrente.
     */
    private Mono<ValidationData> fetchValidationData(Application applicationRequest) {
        Mono<LoanType> loanTypeMono = loanTypeGateway.findById(applicationRequest.getLoanTypeId())
                .switchIfEmpty(Mono.error(new BusinessException("El tipo de préstamo especificado no existe.")));

        Mono<UserRecord> userMono = userGateway.findUserByEmail(applicationRequest.getEmail())
                .switchIfEmpty(Mono.error(new BusinessException("El usuario con el email especificado no existe.")));

        // Se usa el campo inyectado para el ID del estado pendiente
        Mono<Status> initialStatusMono = statusGateway.findById(pendingStatusId)
                .switchIfEmpty(Mono.error(new BusinessException("El estado inicial 'Pendiente' no está configurado en el sistema.")));

        return Mono.zip(loanTypeMono, userMono, initialStatusMono)
                .map(tuple -> new ValidationData(tuple.getT1(), tuple.getT2(), tuple.getT3()));
    }

    /**
     * Paso 2: Ejecuta las validaciones de negocio.
     * Devuelve un Mono con los mismos datos si las validaciones pasan, o un Mono.error si fallan.
     */
    private Mono<ValidationData> validateBusinessRules(Application applicationRequest, ValidationData data) {
        return Mono.fromRunnable(() -> {
            // Se usa el campo inyectado para el ID del rol de cliente
            ApplicationValidator.validateUserRole(data.user(), clientRoleId);
            ApplicationValidator.validateLoanAmount(applicationRequest, data.loanType());
        }).thenReturn(data);
    }

    /**
     * Paso 3: Persiste la nueva solicitud y construye el objeto de respuesta final.
     */
    private Mono<ApplicationCreationResult> saveApplicationAndBuildResult(Application applicationRequest, ValidationData data) {
        Application applicationToSave = new Application(
                null, // Dejar que la BD genere el UUID
                applicationRequest.getAmount(),
                applicationRequest.getTerm(),
                applicationRequest.getEmail(),
                data.initialStatus().getStatusId(),
                data.loanType().getLoanTypeId()
        );

        return applicationRepository.save(applicationToSave)
                .map(savedApplication -> new ApplicationCreationResult(
                        savedApplication,
                        data.loanType(),
                        data.initialStatus(),
                        data.user()
                ));
    }
}
