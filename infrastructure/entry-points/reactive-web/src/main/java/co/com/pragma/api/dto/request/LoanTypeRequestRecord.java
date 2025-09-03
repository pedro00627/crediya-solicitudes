package co.com.pragma.api.dto.request;

public record LoanTypeRequestRecord(
        int loanTypeId,
        String name,
        java.math.BigDecimal minAmount,
        java.math.BigDecimal maxAmount,
        java.math.BigDecimal interestRate,
        boolean autoValidation
) {
}