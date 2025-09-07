package co.com.pragma.model.loantype;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

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
