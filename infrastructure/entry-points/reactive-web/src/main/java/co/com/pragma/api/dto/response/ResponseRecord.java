package co.com.pragma.api.dto.response;

public record ResponseRecord(
        ApplicationResponseRecord applicationResponseRecord,
        StatusResponseRecord statusResponseRecord,
        LoanTypeResponseRecord loanTypeResponseRecord
) {
}
