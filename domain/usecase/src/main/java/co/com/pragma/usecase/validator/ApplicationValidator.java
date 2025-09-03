package co.com.pragma.usecase.validator;

import co.com.pragma.model.application.Application;
import co.com.pragma.model.exception.BusinessException;
import co.com.pragma.model.loantype.LoanType;
import co.com.pragma.model.user.UserRecord;

import java.math.BigDecimal;

public class ApplicationValidator {

    /**
     * Valida que el rol de un usuario corresponda al ID de rol requerido.
     * Lanza una BusinessException si el rol no es el correcto.
     *
     * @param user           El registro del usuario a validar.
     * @param requiredRoleId El ID del rol que se espera que tenga el usuario.
     */
    public static void validateUserRole(UserRecord user, Integer requiredRoleId) {
        if (user == null || !user.getRoleId().equals(requiredRoleId)) {
            throw new BusinessException("El usuario no tiene el rol requerido para esta operación.");
        }
    }

    /**
     * Valida que el monto de la solicitud esté dentro de los límites del tipo de préstamo.
     */
    public static void validateLoanAmount(Application application, LoanType loanType) {
        BigDecimal amount = application.getAmount();
        if (amount.compareTo(loanType.getMinAmount()) < 0 || amount.compareTo(loanType.getMaxAmount()) > 0) {
            throw new BusinessException("El monto solicitado está fuera de los límites para el tipo de préstamo seleccionado.");
        }
    }
}