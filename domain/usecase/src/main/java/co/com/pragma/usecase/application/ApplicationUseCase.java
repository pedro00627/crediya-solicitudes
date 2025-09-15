package co.com.pragma.usecase.application;

import co.com.pragma.model.application.ApplicationCreationResult;
import co.com.pragma.model.application.Application;
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
import co.com.pragma.model.validation.ValidationData;
import co.com.pragma.usecase.validator.ApplicationValidator;
import reactor.core.publisher.Mono;

import java.util.Objects;

public class ApplicationUseCase implements CreateLoanApplicationUseCase {

    private final LoanTypeGateway loanTypeGateway;
    private final StatusGateway statusGateway;
    private final UserGateway userGateway;
    private final ApplicationGateway applicationGateway;
    private final LoggerPort logger;
    private final AppRules appRules;

    public ApplicationUseCase(LoanTypeGateway loanTypeGateway, StatusGateway statusGateway, UserGateway userGateway, ApplicationGateway applicationGateway, LoggerPort logger, AppRules appRules) {
        this.loanTypeGateway = loanTypeGateway;
        this.statusGateway = statusGateway;
        this.userGateway = userGateway;
        this.applicationGateway = applicationGateway;
        this.logger = logger;
        this.appRules = Objects.requireNonNull(appRules, "El objeto de reglas de negocio (AppRules) no puede ser nulo.");
        Objects.requireNonNull(appRules.terminalStatusIds(), "La lista de estados terminales (app.rules.terminal-status-ids) no puede ser nula.");
    }

    /**
     * Orquesta la creación de una solicitud de préstamo, validando datos y reglas de negocio.
     */
    @Override
    public Mono<ApplicationCreationResult> createLoanApplication(Application applicationRequest) {
        logger.info("Iniciando proceso. Email: {}, Request inicial: statusId={}, loanTypeId={}",
                logger.maskEmail(applicationRequest.getEmail()), applicationRequest.getStatusId(), applicationRequest.getLoanTypeId());

        return fetchAndValidateRequiredData(applicationRequest)
                .flatMap(validationData -> validateBusinessRules(applicationRequest, validationData))
                .flatMap(this::checkForOpenApplications)
                .flatMap(validatedData -> fetchDefaultInitialStatus()
                        .flatMap(defaultStatus -> saveApplicationAndBuildResult(applicationRequest, validatedData, defaultStatus)));
    }

    /**
     * Paso 1: Obtiene y valida datos externos de forma concurrente.
     */
    private Mono<ValidationData> fetchAndValidateRequiredData(Application applicationRequest) {
        logger.debug("Paso 1 - fetchAndValidateRequiredData. Request: statusId={}, loanTypeId={}, email={}",
                applicationRequest.getStatusId(), applicationRequest.getLoanTypeId(), logger.maskEmail(applicationRequest.getEmail()));

        Mono<LoanType> loanTypeMono = findAndValidateLoanType(applicationRequest.getLoanTypeId());
        Mono<UserRecord> userMono = findAndValidateUser(applicationRequest.getEmail());

        return Mono.zip(loanTypeMono, userMono)
                .map(tuple -> new ValidationData(tuple.getT1(), tuple.getT2()));
    }

    private Mono<LoanType> findAndValidateLoanType(Integer loanTypeId) {
        logger.debug("Buscando y validando LoanType con ID: {}", loanTypeId);
        return findOrThrow(loanTypeGateway.findById(loanTypeId), BusinessMessages.LOAN_TYPE_NOT_FOUND)
                .doOnSuccess(loanType -> logger.debug("LoanType encontrado y validado: {}", loanType.getName()));
    }

    private Mono<UserRecord> findAndValidateUser(String email) {
        logger.debug("Buscando y validando User con email: {}", logger.maskEmail(email));
        return findOrThrow(userGateway.findUserByEmail(email), BusinessMessages.USER_NOT_FOUND)
                .doOnSuccess(user -> logger.debug("User encontrado y validado: documentId={}", logger.maskDocument(user.getIdentityDocument())));
    }

    /**
     * Busca el estado por defecto para una nueva solicitud (PENDIENTE).
     */
    private Mono<Status> fetchDefaultInitialStatus() {
        logger.debug("Buscando estado por defecto (PENDIENTE) con ID: {}", appRules.pendingStatusId());
        return findOrThrow(statusGateway.findById(appRules.pendingStatusId()), BusinessMessages.INITIAL_STATUS_NOT_FOUND)
                .doOnSuccess(status -> logger.debug("Estado por defecto encontrado: {}", status.getName()));
    }

    /**
     * Valida que el cliente no tenga solicitudes de préstamo abiertas.
     */
    private Mono<ValidationData> checkForOpenApplications(ValidationData data) {
        return Mono.just(data)
                .filter(validationData -> validationData.user() != null && validationData.user().getIdentityDocument() != null && !validationData.user().getIdentityDocument().isBlank())
                .switchIfEmpty(Mono.error(new BusinessException("No se pudo obtener un documento de identidad válido del usuario.")))
                .flatMap(validationData -> {
                    String documentId = validationData.user().getIdentityDocument();
                    logger.debug("Paso 1.5: Verificando solicitudes abiertas para el usuario: {} con documentId: {}",
                            logger.maskEmail(validationData.user().getEmail()), logger.maskDocument(documentId));

                    return applicationGateway.findOpenApplicationsByDocumentId(validationData.user().getIdentityDocument(), appRules.terminalStatusIds())
                            .hasElements()
                            .flatMap(hasOpen -> Boolean.TRUE.equals(hasOpen)
                                    ? Mono.error(new BusinessException(BusinessMessages.USER_HAS_ACTIVE_APPLICATION))
                                    : Mono.just(validationData));
                });
    }

    /**
     * Paso 2: Valida las reglas de negocio como el rol del usuario y el monto del préstamo.
     */
    private Mono<ValidationData> validateBusinessRules(Application applicationRequest, ValidationData data) {
        logger.debug("Paso 2 - validateBusinessRules. Request: statusId={}, loanTypeId={}. Validando para usuario: {}",
                applicationRequest.getStatusId(), applicationRequest.getLoanTypeId(), logger.maskEmail(data.user().getEmail()));
        return Mono.fromCallable(() -> {
            ApplicationValidator.validateUserRole(data.user(), appRules.clientRoleId());
            ApplicationValidator.validateLoanAmount(applicationRequest, data.loanType());
            return data;
        });
    }

    /**
     * Paso 3: Guarda la solicitud y construye el resultado final.
     */
    private Mono<ApplicationCreationResult> saveApplicationAndBuildResult(Application applicationRequest, ValidationData data, Status defaultStatus) {
        logger.debug("Paso 3 - saveApplicationAndBuildResult. Request original: statusId={}, loanTypeId={}. Estado por defecto a usar: statusId={}",
                applicationRequest.getStatusId(), applicationRequest.getLoanTypeId(), defaultStatus.getStatusId());
        Application applicationToSave = new Application(
                null,
                data.user().getIdentityDocument(),
                applicationRequest.getAmount(),
                applicationRequest.getTerm(),
                applicationRequest.getEmail(),
                defaultStatus.getStatusId(),
                data.loanType().getLoanTypeId()
        );

        return applicationGateway.save(applicationToSave)
                .map(savedApplication -> new ApplicationCreationResult(savedApplication, data.loanType(), defaultStatus, data.user()));
    }

    private <T> Mono<T> findOrThrow(Mono<T> source, String errorMessage) {
        return source.switchIfEmpty(Mono.error(new BusinessException(errorMessage)));
    }
}
