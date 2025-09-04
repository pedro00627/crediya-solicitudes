package co.com.pragma.model.application;

import co.com.pragma.model.loantype.LoanType;
import co.com.pragma.model.status.Status;
import co.com.pragma.model.user.UserRecord;

/**
 * Representa el resultado completo y enriquecido tras crear una solicitud de préstamo.
 * Renombrada de LoanCreationResponse para cumplir con las reglas de arquitectura.
 */
public record ApplicationCreationResult(
        Application application,
        LoanType loanType,
        Status status,
        UserRecord user
) {
}