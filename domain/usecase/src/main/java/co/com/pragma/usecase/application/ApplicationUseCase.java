package co.com.pragma.usecase.application;

import co.com.pragma.model.application.Application;
import co.com.pragma.model.application.ApplicationCreationResult;
import co.com.pragma.model.application.gateways.ApplicationRepository;
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
import co.com.pragma.model.validation.ValidationData;
import co.com.pragma.usecase.validator.ApplicationValidator;
import reactor.core.publisher.Mono;

import java.util.Objects;

// Esta clase NO debe tener anotaciones de Spring como @Component o @UseCase.
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
     * Orquesta la creación de una solicitud de préstamo, validando datos y reglas de negocio.
     */
    public Mono<ApplicationCreationResult> createLoanApplication(Application applicationRequest) {
        // LOG DE SEGUIMIENTO 1: Valores iniciales al entrar al UseCase.
        logger.info("Iniciando proceso. Email: {}, Request inicial: statusId={}, loanTypeId={}",
                logger.maskEmail(applicationRequest.getEmail()), applicationRequest.getStatusId(), applicationRequest.getLoanTypeId());
        // El flujo se corrige para que las validaciones de negocio ocurran antes de asignar un estado.
        // El estado inicial 'PENDIENTE' es una regla del servidor, no una entrada del cliente.
        return fetchAndValidateRequiredData(applicationRequest)
                .flatMap(validationData -> validateBusinessRules(applicationRequest, validationData))
                .flatMap(this::checkForOpenApplications) // Primero se valida si hay solicitudes abiertas
                // La obtención del estado por defecto se mueve dentro de un flatMap para asegurar
                // que solo se ejecute después de que todas las validaciones anteriores hayan pasado.
                .flatMap(validatedData -> fetchDefaultInitialStatus()
                        .flatMap(defaultStatus -> saveApplicationAndBuildResult(applicationRequest, validatedData, defaultStatus)));
    }

    /**
     * Paso 1: Obtiene y valida datos externos de forma concurrente.
     */
    private Mono<ValidationData> fetchAndValidateRequiredData(Application applicationRequest) {
        // LOG DE SEGUIMIENTO 2: Verificamos los IDs que se usarán para las consultas externas.
        logger.debug("Paso 1 - fetchAndValidateRequiredData. Request: statusId={}, loanTypeId={}, email={}",
                applicationRequest.getStatusId(), applicationRequest.getLoanTypeId(), logger.maskEmail(applicationRequest.getEmail()));

        Mono<LoanType> loanTypeMono = findAndValidateLoanType(applicationRequest.getLoanTypeId());
        Mono<UserRecord> userMono = findAndValidateUser(applicationRequest.getEmail());

        return Mono.zip(loanTypeMono, userMono)
                .map(tuple -> new ValidationData(tuple.getT1(), tuple.getT2()));
    }

    private Mono<LoanType> findAndValidateLoanType(Integer loanTypeId) {
        // Se añade logging para trazar la entrada y salida del método.
        logger.debug("Buscando y validando LoanType con ID: {}", loanTypeId);
        return findOrThrow(loanTypeGateway.findById(loanTypeId), BusinessMessages.LOAN_TYPE_NOT_FOUND)
                .doOnSuccess(loanType -> logger.debug("LoanType encontrado y validado: {}", loanType.getName()));
    }

    private Mono<UserRecord> findAndValidateUser(String email) {
        // Se añade logging para trazar la entrada y salida del método.
        logger.debug("Buscando y validando User con email: {}", logger.maskEmail(email));
        return findOrThrow(userGateway.findUserByEmail(email), BusinessMessages.USER_NOT_FOUND)
                .doOnSuccess(user -> logger.debug("User encontrado y validado: documentId={}", user.getIdentityDocument()));
    }

    /**
     * Busca el estado por defecto para una nueva solicitud (PENDIENTE).
     */
    private Mono<Status> fetchDefaultInitialStatus() {
        // Se añade logging para trazar la entrada y salida del método.
        logger.debug("Buscando estado por defecto (PENDIENTE) con ID: {}", appRules.pendingStatusId());
        return findOrThrow(statusGateway.findById(appRules.pendingStatusId()), BusinessMessages.INITIAL_STATUS_NOT_FOUND)
                .doOnSuccess(status -> logger.debug("Estado por defecto encontrado: {}", status.getName()));
    }

    /**
     * Valida que el cliente no tenga solicitudes de préstamo abiertas.
     */
    private Mono<ValidationData> checkForOpenApplications(ValidationData data) {
        // Se envuelve en un Mono para aplicar una validación reactiva y idiomática.
        return Mono.just(data)
                // Guardia reactiva: se asegura de que el usuario y su documento de identidad sean válidos.
                // Esto previene NullPointerExceptions y consultas con datos inválidos.
                .filter(validationData -> validationData.user() != null && validationData.user().getIdentityDocument() != null && !validationData.user().getIdentityDocument().isBlank())
                .switchIfEmpty(Mono.error(new BusinessException("No se pudo obtener un documento de identidad válido del usuario.")))
                .flatMap(validationData -> {
                    // LOG DE SEGUIMIENTO CRÍTICO: Verificamos el documento exacto que se usa en la consulta.
                    String documentId = validationData.user().getIdentityDocument();
                    logger.debug("Paso 1.5: Verificando solicitudes abiertas para el usuario: {} con documentId: {}",
                            logger.maskEmail(validationData.user().getEmail()), documentId);

                    return applicationRepository.findOpenApplicationsByDocumentId(validationData.user().getIdentityDocument(), appRules.terminalStatusIds())
                            .hasElements()
                            .flatMap(hasOpen -> Boolean.TRUE.equals(hasOpen)
                                    ? Mono.error(new BusinessException(BusinessMessages.CLIENT_HAS_OPEN_APPLICATION))
                                    : Mono.just(validationData)); // Continúa el flujo si no hay solicitudes abiertas.
                });
    }

    /**
     * Paso 2: Valida las reglas de negocio como el rol del usuario y el monto del préstamo.
     */
    private Mono<ValidationData> validateBusinessRules(Application applicationRequest, ValidationData data) {
        // LOG DE SEGUIMIENTO 3: Verificamos el request justo antes de las validaciones de negocio.
        logger.debug("Paso 2 - validateBusinessRules. Request: statusId={}, loanTypeId={}. Validando para usuario: {}",
                applicationRequest.getStatusId(), applicationRequest.getLoanTypeId(), logger.maskEmail(data.user().getEmail()));
        // Se usa Mono.fromCallable para envolver la lógica de validación síncrona de forma reactiva.
        return Mono.fromCallable(() -> {
            ApplicationValidator.validateUserRole(data.user(), appRules.clientRoleId());
            ApplicationValidator.validateLoanAmount(applicationRequest, data.loanType());
            return data; // Devuelve el objeto si las validaciones pasan.
        });
    }

    /**
     * Paso 3: Guarda la solicitud y construye el resultado final.
     */
    private Mono<ApplicationCreationResult> saveApplicationAndBuildResult(Application applicationRequest, ValidationData data, Status defaultStatus) {
        // LOG DE SEGUIMIENTO 4: Verificamos el request y el estado por defecto justo antes de guardar.
        logger.debug("Paso 3 - saveApplicationAndBuildResult. Request original: statusId={}, loanTypeId={}. Estado por defecto a usar: statusId={}",
                applicationRequest.getStatusId(), applicationRequest.getLoanTypeId(), defaultStatus.getStatusId());
        Application applicationToSave = new Application(
                null, // El ID se genera en la capa de aplicación.
                data.user().getIdentityDocument(), // ¡Aquí está la clave!
                applicationRequest.getAmount(),
                applicationRequest.getTerm(),
                applicationRequest.getEmail(),
                defaultStatus.getStatusId(),
                data.loanType().getLoanTypeId()
        );

        return applicationRepository.save(applicationToSave)
                .map(savedApplication -> new ApplicationCreationResult(savedApplication, data.loanType(), defaultStatus, data.user()));
    }

    /**
     * Helper genérico para el patrón "buscar o lanzar excepción de negocio".
     */
    private <T> Mono<T> findOrThrow(Mono<T> source, String errorMessage) {
        return source.switchIfEmpty(Mono.error(new BusinessException(errorMessage)));
    }
}