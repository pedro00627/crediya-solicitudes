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

import java.util.UUID;

@RequiredArgsConstructor
public class ApplicationUseCase {

    // Gateways to external data/services
    private final LoanTypeGateway loanTypeGateway;
    private final StatusGateway statusGateway;
    private final UserGateway userGateway;

    // Repository for our own aggregate
    private final ApplicationRepository applicationRepository;

    // Constants for business rules
    private static final Integer CLIENT_ROLE_ID = 2; // Example: Assuming 2 is the ID for "Cliente"
    private static final int PENDING_STATUS_ID = 1; // Example: Assuming 1 is "Pendiente de revisión"

    /* validar cliente, consultar el rol del usuario desde la api
     * - Se puede enviar una solicitud de crédito que incluya información del cliente (documento de identidad)
     *  y los detalles del préstamo (monto, plazo), el tipo de prestamos que se quiere hacer
     * - La solicitud se registra automáticamente con un estado inicial de "Pendiente de revisión".
     * - El sistema valida que el tipo de préstamo seleccionado sea uno de los tipos de préstamo existentes.*/
    /**
     * Orquesta el proceso de creación de una nueva solicitud de préstamo y devuelve una respuesta enriquecida.
     * 1. Obtiene los datos de validación (Usuario, Tipo de Préstamo, Estado Inicial).
     * 2. Ejecuta las reglas de negocio (rol, monto).
     * 3. Guarda la nueva solicitud.
     * 4. Construye y devuelve el objeto de respuesta completo.
     *
     * @param applicationRequest Los datos de la solicitud entrante.
     * @return Un Mono que emite la respuesta enriquecida de la creación.
     */
    public Mono<ApplicationCreationResult> createLoanApplication(Application applicationRequest) {
        // 1. Fetch all required data concurrently
        Mono<LoanType> loanTypeMono = loanTypeGateway.findById(applicationRequest.loanTypeId())
                .switchIfEmpty(Mono.error(new BusinessException("El tipo de préstamo especificado no existe.")));

        Mono<UserRecord> userMono = userGateway.findUserByEmail(applicationRequest.email())
                .switchIfEmpty(Mono.error(new BusinessException("El usuario con el email especificado no existe.")));

        Mono<Status> initialStatusMono = statusGateway.findById(PENDING_STATUS_ID)
                .switchIfEmpty(Mono.error(new BusinessException("El estado inicial 'Pendiente' no está configurado en el sistema.")));

        return Mono.zip(loanTypeMono, userMono, initialStatusMono)
                .flatMap(tuple -> {
                    LoanType loanType = tuple.getT1();
                    UserRecord user = tuple.getT2();
                    Status initialStatus = tuple.getT3();

                    // 2. Perform business rule validations
                    ApplicationValidator.validateUserRole(user, CLIENT_ROLE_ID);
                    ApplicationValidator.validateLoanAmount(applicationRequest, loanType);

                    // 3. Create the definitive application object to be saved
                    Application applicationToSave = new Application(
                            UUID.randomUUID(), // Generate a new unique ID
                            applicationRequest.amount(),
                            applicationRequest.term(),
                            applicationRequest.email(),
                            initialStatus.statusId(), // Use the ID from the fetched status
                            loanType.loanTypeId()
                    );

                    // 4. Persist the new application and then build the final response
                    return applicationRepository.save(applicationToSave)
                            .map(savedApplication -> new ApplicationCreationResult(savedApplication, loanType, initialStatus, user));
                });
    }
}
