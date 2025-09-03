package co.com.pragma.model.loantype;

public record LoanType(
        int loanTypeId,
        String name,
        java.math.BigDecimal minAmount,
        java.math.BigDecimal maxAmount,
        java.math.BigDecimal interestRate,
        boolean autoValidation
) {
}
