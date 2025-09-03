package co.com.pragma.api.dto.request;

import reactor.util.annotation.NonNull;

public record RequestRecord(
        @NonNull
        ApplicationRequestRecord applicationRequestRecord,
        @NonNull
        LoanTypeRequestRecord loanTypeRequestRecord
) {
}
