package co.com.pragma.model.application;

import java.math.BigDecimal;
import java.util.UUID;

public record Application(
        UUID id,
        BigDecimal amount,
        Integer term,
        String email,
        Integer statusId,
        Integer loanTypeId
) {
}