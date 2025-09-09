package co.com.pragma.api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

public record ApplicationRequestRecord(
        java.util.UUID applicationId,
        @NotNull(message = "El monto no puede ser nulo")
        java.math.BigDecimal amount,
        @NotNull(message = "El plazo no puede ser nulo")
        int term,
        @Email(message = "Debe ser una dirección de correo electrónico válida")
        String email,
        @NotNull(message = "El nombre del estado no puede ser nulo")
        String statusName,
        @NotNull(message = "El nombre del tipo de préstamo no puede ser nulo")
        String loanTypeName
) {
}
