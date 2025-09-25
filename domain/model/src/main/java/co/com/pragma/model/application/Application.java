package co.com.pragma.model.application;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Application {
    private UUID applicationId;
    private String documentId;
    private BigDecimal amount;
    private int term;
    private String email;
    private int statusId;
    private int loanTypeId;

    // Campos de evaluación (nullable en creación, obligatorios en aprobación/rechazo)
    private String advisorId;
    private String decisionReason;
    private BigDecimal appliedInterestRate;
    private BigDecimal monthlyPayment;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Métodos de negocio
    public boolean isPending() {
        return statusId == 1; // Asumiendo que 1 = PENDIENTE
    }

    public boolean isApproved() {
        return statusId == 2; // Asumiendo que 2 = APROBADA
    }

    public boolean isRejected() {
        return statusId == 3; // Asumiendo que 3 = RECHAZADA
    }

    public Application approve(String advisorId, String reason, BigDecimal interestRate, BigDecimal monthlyPayment) {
        return this.toBuilder()
                .statusId(2) // APROBADA
                .advisorId(advisorId)
                .decisionReason(reason)
                .appliedInterestRate(interestRate)
                .monthlyPayment(monthlyPayment)
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public Application reject(String advisorId, String reason) {
        return this.toBuilder()
                .statusId(3) // RECHAZADA
                .advisorId(advisorId)
                .decisionReason(reason)
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
