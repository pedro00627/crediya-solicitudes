package co.com.pragma.model.validation;

import co.com.pragma.model.loantype.LoanType;
import co.com.pragma.model.status.Status;
import co.com.pragma.model.user.UserRecord;

/**
 * Un record inmutable que agrupa todos los datos necesarios para la validación de una solicitud.
 * Al ser un record, es conciso y seguro por defecto.
 */
public record ValidationData(LoanType loanType, UserRecord user, Status initialStatus) {
}