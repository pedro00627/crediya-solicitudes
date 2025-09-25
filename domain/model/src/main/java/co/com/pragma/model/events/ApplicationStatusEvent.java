package co.com.pragma.model.events;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Evento de cambio de estado de solicitud para notificaciones
 * Usado para HU6: Listado de solicitudes de un cliente
 */
public record ApplicationStatusEvent(
        String applicationId,
        String userId,
        String userEmail,
        String userPhone,
        String userName,
        String previousStatus,
        String newStatus,
        BigDecimal amount,
        String loanType,
        Integer termMonths,
        String advisorId,
        String advisorName,
        String rejectionReason,
        LocalDateTime timestamp
) {

    public static ApplicationStatusEvent create(
            String applicationId,
            String userId,
            String userEmail,
            String userPhone,
            String userName,
            String previousStatus,
            String newStatus,
            BigDecimal amount,
            String loanType,
            Integer termMonths,
            String advisorId,
            String advisorName,
            String rejectionReason) {

        return new ApplicationStatusEvent(
                applicationId,
                userId,
                userEmail,
                userPhone,
                userName,
                previousStatus,
                newStatus,
                amount,
                loanType,
                termMonths,
                advisorId,
                advisorName,
                rejectionReason,
                LocalDateTime.now()
        );
    }
/*
    public boolean isApproved() {
        return ApplicationStatusConstants.APPROVED.equals(newStatus);
    }

    public boolean isRejected() {
        return ApplicationStatusConstants.REJECTED.equals(newStatus);
    }

    public boolean isPending() {
        return ApplicationStatusConstants.PENDING.equals(newStatus);
    }

    public boolean isUnderReview() {
        return ApplicationStatusConstants.UNDER_REVIEW.equals(newStatus);
    }*/
}