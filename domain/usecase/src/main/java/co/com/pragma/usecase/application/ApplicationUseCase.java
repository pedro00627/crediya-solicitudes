package co.com.pragma.usecase.application;

import co.com.pragma.model.application.Application;
import co.com.pragma.model.application.ApplicationCreationResult;
import co.com.pragma.model.config.AppRules;
import co.com.pragma.model.application.gateways.ApplicationRepository;
import co.com.pragma.model.exception.BusinessException;
import co.com.pragma.model.loantype.LoanType;
import co.com.pragma.model.loantype.gateways.LoanTypeGateway;
import co.com.pragma.model.log.gateways.LoggerPort;
import co.com.pragma.model.status.Status;
import co.com.pragma.model.status.gateways.StatusGateway;
import co.com.pragma.model.validation.ValidationData;
import co.com.pragma.model.user.UserRecord;
import co.com.pragma.model.user.gateways.UserGateway;
import co.com.pragma.usecase.validator.ApplicationValidator;
import reactor.core.publisher.Mono;

import java.util.Objects;

// Esta clase NO debe tener anotaciones de Spring como @Component o @UseCase.
// Su creación es gestionada explícitamente por el método @Bean en UseCasesConfig.java,
// lo que evita conflictos de definición y mantiene el dominio puro.
public class ApplicationUseCase {

    // Gateways
    private final LoanTypeGateway loanTypeGateway;
    private final StatusGateway statusGateway;
    private final UserGateway userGateway;
    private final ApplicationRepository applicationRepository;
    private final LoggerPort logger;

    // Las reglas de negocio ahora se agrupan en un único objeto para mayor claridad.
    private final AppRules appRules;

    public ApplicationUseCase(LoanTypeGateway loanTypeGateway, StatusGateway statusGateway, UserGateway userGateway, ApplicationRepository applicationRepository, LoggerPort logger, AppRules appRules) {
        this.loanTypeGateway = loanTypeGateway;
        this.statusGateway = statusGateway;
        this.userGateway = userGateway;
        this.applicationRepository = applicationRepository;
        this.logger = logger;
        // Validación "Fail-Fast": Asegura que la configuración crítica exista al iniciar.
        this.appRules = Objects.requireNonNull(appRules, "El objeto de reglas de negocio (AppRules) no puede ser nulo.");
        Objects.requireNonNull(appRules.terminalStatusIds(), "La lista de estados terminales (app.rules.terminal-status-ids) no puede ser nula.");
    }

    /**
     * Orquesta el proceso de creación de una nueva solicitud de préstamo.
     * El flujo es declarativo: obtener datos, validar, y luego guardar.
     */
    public Mono<ApplicationCreationResult> createLoanApplication(Application applicationRequest) {
        logger.info("Iniciando proceso de creación de solicitud para el email: {}", logger.maskEmail(applicationRequest.getEmail()));        return fetchAndValidateRequiredData(applicationRequest)
                .flatMap(this::checkForOpenApplications)
                .flatMap(validationData -> validateBusinessRules(applicationRequest, validationData))
                .flatMap(validationData -> saveApplicationAndBuildResult(applicationRequest, validationData));
    }

    /**
     * Paso 1: Obtiene y valida la existencia de todos los datos externos necesarios de forma concurrente.
     */
    private Mono<ValidationData> fetchAndValidateRequiredData(Application applicationRequest) {
        logger.debug("Paso 1: Obteniendo y validando existencia de datos para la solicitud...");

        Mono<LoanType> loanTypeMono = findAndValidateLoanType(applicationRequest.getLoanTypeId());
        Mono<UserRecord> userMono = findAndValidateUser(applicationRequest.getEmail());
        Mono<Status> initialStatusMono = findAndValidateInitialStatus();

        return Mono.zip(loanTypeMono, userMono, initialStatusMono)
                .map(tuple -> new ValidationData(tuple.getT1(), tuple.getT2(), tuple.getT3()));
    }

    private Mono<LoanType> findAndValidateLoanType(Integer loanTypeId) {
        return loanTypeGateway.findById(loanTypeId)
                .switchIfEmpty(Mono.error(new BusinessException("El tipo de préstamo especificado no existe.")));
    }

    private Mono<UserRecord> findAndValidateUser(String email) {
        // El UseCase confía en que el Gateway devolverá un Mono vacío si el usuario no se encuentra.
        // La responsabilidad de manejar excepciones de red (como un 404) es del Gateway.
        // Aquí, simplemente traducimos la ausencia de un usuario en un error de negocio.
        return userGateway.findUserByEmail(email)
                .switchIfEmpty(Mono.error(new BusinessException("El usuario con el email especificado no existe.")));
    }

    private Mono<Status> findAndValidateInitialStatus() {
        // Se usa el campo inyectado para el ID del estado pendiente
        return statusGateway.findById(appRules.pendingStatusId())
                .switchIfEmpty(Mono.error(new BusinessException("El estado inicial 'Pendiente' no está configurado en el sistema.")));
    }

    /**
     * Paso Intermedio: Valida que el cliente no tenga solicitudes abiertas usando su documentId.
     */
    private Mono<ValidationData> checkForOpenApplications(ValidationData data) {
        String documentId = data.user().getIdentityDocument();
        logger.debug("Paso 1.5: Verificando solicitudes abiertas para el usuario: {}", logger.maskEmail(data.user().getEmail()));        return applicationRepository.findOpenApplicationsByDocumentId(documentId, appRules.terminalStatusIds())
                .hasElements() // Devuelve true si encuentra al menos una solicitud.
                .flatMap(hasOpen -> {
                    if (Boolean.TRUE.equals(hasOpen)) {
                        return Mono.error(new BusinessException("El cliente ya tiene una solicitud de préstamo activa."));
                    }
                    return Mono.just(data); // Si no hay solicitudes abiertas, continúa el flujo.
                });
    }

    /**
     * Paso 2: Ejecuta las validaciones de negocio.
     * Devuelve un Mono con los mismos datos si las validaciones pasan, o un Mono.error si fallan.
     */
    private Mono<ValidationData> validateBusinessRules(Application applicationRequest, ValidationData data) {
        logger.debug("Paso 2: Validando reglas de negocio para el usuario: {}", logger.maskEmail(data.user().getEmail()));        return Mono.fromRunnable(() -> {
            // Se usa el campo inyectado para el ID del rol de cliente
            ApplicationValidator.validateUserRole(data.user(), appRules.clientRoleId());
            ApplicationValidator.validateLoanAmount(applicationRequest, data.loanType());
        }).thenReturn(data);
    }

    /**
     * Paso 3: Persiste la nueva solicitud y construye el objeto de respuesta final.
     */
    private Mono<ApplicationCreationResult> saveApplicationAndBuildResult(Application applicationRequest, ValidationData data) {
        logger.debug("Paso 3: Guardando la solicitud para el usuario: {}", logger.maskEmail(data.user().getEmail()));        Application applicationToSave = new Application(
                null, // El ID se genera en la capa de aplicación.
                data.user().getIdentityDocument(), // ¡Aquí está la clave!
                applicationRequest.getAmount(),
                applicationRequest.getTerm(),
                applicationRequest.getEmail(),
                data.initialStatus().getStatusId(),
                data.loanType().getLoanTypeId()
        );

        return applicationRepository.save(applicationToSave)
                .map(savedApplication -> new ApplicationCreationResult(
                        savedApplication, data.loanType(), data.initialStatus(), data.user()
                ));
    }
}