package co.com.pragma.model.loantype;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Representa un tipo de préstamo.
 * Convertido de un record a una clase para compatibilidad con librerías
 * que requieren un constructor sin argumentos.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoanType {
    private int loanTypeId;
    private String name;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    private BigDecimal interestRate;
    private boolean autoValidation;
}
