package co.com.pragma.model.application;

import co.com.pragma.model.loantype.LoanType;
import co.com.pragma.model.status.Status;
import co.com.pragma.model.user.UserRecord;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Representa la respuesta completa y enriquecida tras crear una solicitud de préstamo.
 * Convertido de un record a una clase para compatibilidad con librerías
 * que requieren un constructor sin argumentos.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationCreationResult {
    private Application application;
    private LoanType loanType;
    private Status status;
    private UserRecord user;
}
