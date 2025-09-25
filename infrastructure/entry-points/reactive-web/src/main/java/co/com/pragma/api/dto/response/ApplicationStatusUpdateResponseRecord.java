package co.com.pragma.api.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ApplicationStatusUpdateResponseRecord(
        String applicationId,
        String status,
        String advisorId,
        String reason,
        BigDecimal appliedInterestRate,
        BigDecimal monthlyPayment,
        LocalDateTime updatedAt,
        String message
) {
    public static ApplicationStatusUpdateResponseRecord approved(
            String applicationId,
            String advisorId,
            String reason,
            BigDecimal interestRate,
            BigDecimal monthlyPayment,
            LocalDateTime updatedAt) {
        return new ApplicationStatusUpdateResponseRecord(
                applicationId,
                "APROBADA",
                advisorId,
                reason,
                interestRate,
                monthlyPayment,
                updatedAt,
                "Solicitud aprobada exitosamente"
        );
    }

    public static ApplicationStatusUpdateResponseRecord rejected(
            String applicationId,
            String advisorId,
            String reason,
            LocalDateTime updatedAt) {
        return new ApplicationStatusUpdateResponseRecord(
                applicationId,
                "RECHAZADA",
                advisorId,
                reason,
                null,
                null,
                updatedAt,
                "Solicitud rechazada"
        );
    }
}