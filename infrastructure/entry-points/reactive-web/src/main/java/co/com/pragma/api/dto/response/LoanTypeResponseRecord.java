package co.com.pragma.api.dto.response;

public record LoanTypeResponseRecord(
        Integer loanTypeId,
        String name,
        java.math.BigDecimal minAmount,
        java.math.BigDecimal maxAmount,
        java.math.BigDecimal interestRate,
        boolean autoValidation
) {
}