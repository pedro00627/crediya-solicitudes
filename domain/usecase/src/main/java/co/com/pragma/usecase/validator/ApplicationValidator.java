package co.com.pragma.usecase.validator;

import co.com.pragma.model.application.Application;
import co.com.pragma.model.constants.ValidationMessages;
import co.com.pragma.model.exception.BusinessException;
import co.com.pragma.model.loantype.LoanType;
import co.com.pragma.model.user.UserRecord;

import java.math.BigDecimal;

public enum ApplicationValidator {
    ;


    /**
     * Valida que el rol de un usuario corresponda al ID de rol requerido.
     * Lanza una BusinessException si el rol no es el correcto.
     *
     * @param user           El registro del usuario a validar.
     * @param requiredRoleId El ID del rol que se espera que tenga el usuario.
     */
    public static void validateUserRole(final UserRecord user, final Integer requiredRoleId) {
        if (null == user || null == user.getRoleId() || !user.getRoleId().equals(requiredRoleId)) {
            throw new BusinessException(ValidationMessages.INVALID_USER_ROLE);
        }
    }

    /**
     * Valida que el monto de la solicitud esté dentro de los límites del tipo de préstamo.
     */
    public static void validateLoanAmount(final Application application, final LoanType loanType) {
        final BigDecimal amount = application.getAmount();
        if (0 > amount.compareTo(loanType.getMinAmount()) || 0 < amount.compareTo(loanType.getMaxAmount())) {
            throw new BusinessException(ValidationMessages.LOAN_AMOUNT_OUT_OF_RANGE);
        }
    }


}