package co.com.pragma.model.application;

import co.com.pragma.model.loantype.LoanType;
import co.com.pragma.model.status.Status;
import co.com.pragma.model.user.UserRecord;

/**
 * Representa la respuesta completa y enriquecida tras crear una solicitud de préstamo.
 */
public record ApplicationCreationResult(
        Application application,
        LoanType loanType,
        Status status,
        UserRecord user
) {
}