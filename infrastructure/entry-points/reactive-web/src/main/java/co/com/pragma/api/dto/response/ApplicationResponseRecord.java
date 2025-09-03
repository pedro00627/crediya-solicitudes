package co.com.pragma.api.dto.response;

public record ApplicationResponseRecord(
        java.util.UUID applicationId,
        java.math.BigDecimal amount,
        int term,
        String email,
        int statusId,
        int loanTypeId
) {
}
