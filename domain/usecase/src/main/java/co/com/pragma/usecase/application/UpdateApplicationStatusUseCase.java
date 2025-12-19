package co.com.pragma.usecase.application;

import co.com.pragma.model.application.Application;
import co.com.pragma.model.application.gateways.ApplicationGateway;
import co.com.pragma.model.events.ApplicationStatusEvent;
import co.com.pragma.model.exception.BusinessException;
import co.com.pragma.model.gateways.NotificationGateway;
import co.com.pragma.model.loantype.LoanType;
import co.com.pragma.model.loantype.gateways.LoanTypeGateway;
import co.com.pragma.model.log.gateways.LoggerPort;
import co.com.pragma.model.status.gateways.StatusGateway;
import co.com.pragma.model.user.gateways.UserGateway;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@RequiredArgsConstructor
public class UpdateApplicationStatusUseCase {

    private final ApplicationGateway applicationGateway;
    private final LoanTypeGateway loanTypeGateway;
    private final StatusGateway statusGateway;
    private final UserGateway userGateway;
    private final NotificationGateway notificationGateway;
    private final LoggerPort logger;

    public Mono<Application> updateStatus(UUID applicationId, String advisorId, String newStatus, String reason) {
        return updateStatusTransactional(applicationId, advisorId, newStatus, reason)
                .flatMap(result ->
                    sendNotification(result.originalApp(), result.updatedApp(), advisorId, reason)
                        .doOnSuccess(messageId -> logger.info("Successfully queued notification with messageId: {}", messageId))
                        .doOnError(error -> logger.error("Failed to queue notification for application {}. Rolling back transaction.", error))
                        .thenReturn(result.updatedApp()) // Si la notificación es exitosa, se pasa la aplicación actualizada.
                )
                .doOnNext(app -> logger.info("Estado de solicitud {} actualizado a {} por asesor {}",
                        applicationId, newStatus, advisorId))
                .doOnError(error -> logger.error("Error actualizando estado de solicitud " + applicationId, error));
    }

    private Mono<StatusUpdateResult> updateStatusTransactional(UUID applicationId, String advisorId, String newStatus, String reason) {
        return applicationGateway.findById(applicationId)
                .switchIfEmpty(Mono.error(new BusinessException("Solicitud no encontrada")))
                .flatMap(originalApplication ->
                    validateAndUpdateStatus(originalApplication, advisorId, newStatus, reason)
                            .flatMap(applicationGateway::save)
                            .map(updatedApplication -> new StatusUpdateResult(originalApplication, updatedApplication))
                );
    }

    private record StatusUpdateResult(Application originalApp, Application updatedApp) {}

    private Mono<Application> validateAndUpdateStatus(Application application, String advisorId, String newStatus, String reason) {
        // Validar que la solicitud esté en estado pendiente
        if (!application.isPending()) {
            return Mono.error(new BusinessException("Solo se pueden evaluar solicitudes en estado pendiente"));
        }

        return switch (newStatus.toUpperCase()) {
            case "APROBADA" -> processApproval(application, advisorId, reason);
            case "RECHAZADA" -> processRejection(application, advisorId, reason);
            default -> Mono.error(new BusinessException("Estado inválido: " + newStatus));
        };
    }

    private Mono<Application> processApproval(Application application, String advisorId, String reason) {
        return loanTypeGateway.findById(application.getLoanTypeId())
                .switchIfEmpty(Mono.error(new BusinessException("Tipo de préstamo no encontrado")))
                .map(loanType -> {
                    // Calcular cuota mensual con tasa actual
                    BigDecimal monthlyPayment = calculateMonthlyPayment(
                            application.getAmount(),
                            loanType.getInterestRate(),
                            application.getTerm()
                    );

                    return application.approve(advisorId, reason, loanType.getInterestRate(), monthlyPayment);
                });
    }

    private Mono<Application> processRejection(Application application, String advisorId, String reason) {
        return Mono.just(application.reject(advisorId, reason));
    }

    private BigDecimal calculateMonthlyPayment(BigDecimal principal, BigDecimal annualRate, int termMonths) {
        // Convertir tasa anual a mensual
        BigDecimal monthlyRate = annualRate.divide(BigDecimal.valueOf(12), 10, RoundingMode.HALF_UP);

        // Fórmula de cuota fija: P * [r(1+r)^n] / [(1+r)^n - 1]
        BigDecimal onePlusRate = BigDecimal.ONE.add(monthlyRate);
        BigDecimal onePlusRatePowN = onePlusRate.pow(termMonths);

        BigDecimal numerator = principal.multiply(monthlyRate).multiply(onePlusRatePowN);
        BigDecimal denominator = onePlusRatePowN.subtract(BigDecimal.ONE);

        return numerator.divide(denominator, 2, RoundingMode.HALF_UP);
    }

    private Mono<String> sendNotification(Application originalApplication, Application updatedApplication,
                                        String advisorId, String reason) {
        // Este método ahora se enfoca solo en construir y enviar.
        // El manejo de errores se hace en el suscriptor.
        return buildNotificationData(originalApplication, updatedApplication, advisorId, reason)
                .flatMap(notificationGateway::sendApplicationStatusChange);
    }

    private Mono<ApplicationStatusEvent> buildNotificationData(Application originalApp, Application updatedApp,
                                                             String advisorId, String reason) {
        // Se añade validación para asegurar que los datos necesarios para la notificación existan.
        return userGateway.findUserByEmail(updatedApp.getEmail())
                .switchIfEmpty(Mono.error(() -> new BusinessException("Notification failed: User not found for email " + updatedApp.getEmail())))
                .flatMap(user -> {
                    // Consultas cacheadas (rápidas)
                    return Mono.zip(
                            statusGateway.findById(originalApp.getStatusId()).switchIfEmpty(Mono.error(() -> new BusinessException("Notification failed: Previous status not found"))),
                            statusGateway.findById(updatedApp.getStatusId()).switchIfEmpty(Mono.error(() -> new BusinessException("Notification failed: New status not found"))),
                            loanTypeGateway.findById(updatedApp.getLoanTypeId()).switchIfEmpty(Mono.error(() -> new BusinessException("Notification failed: Loan type not found"))),
                            Mono.just(user) // Usuario ya consultado
                    );
                })
                .map(tuple -> {
                    var previousStatus = tuple.getT1();
                    var newStatus = tuple.getT2();
                    var loanType = tuple.getT3();
                    var user = tuple.getT4();

                    return ApplicationStatusEvent.create(
                            updatedApp.getApplicationId().toString(),
                            user.getId(),
                            user.getEmail(),
                            user.getPhone(),
                            user.getFirstName() + " " + user.getLastName(),
                            previousStatus.getName(),
                            newStatus.getName(),
                            updatedApp.getAmount(),
                            loanType.getName(),
                            updatedApp.getTerm(),
                            advisorId,
                            getAdvisorName(advisorId),
                            reason
                    );
                });
    }

    private String getAdvisorName(String advisorId) {
        // TODO: Obtener el nombre del asesor desde el servicio de autenticación
        // Por ahora devolvemos el ID
        return "Asesor " + advisorId;
    }
}