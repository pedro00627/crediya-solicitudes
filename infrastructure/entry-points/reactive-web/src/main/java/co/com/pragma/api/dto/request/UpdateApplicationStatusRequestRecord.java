package co.com.pragma.api.dto.request;

import co.com.pragma.model.constants.ValidationMessages;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UpdateApplicationStatusRequestRecord(
        @NotBlank(message = ValidationMessages.ADVISOR_ID_REQUIRED)
        String advisorId,

        @NotBlank(message = "El nuevo estado es requerido")
        @Pattern(regexp = "^(APROBADA|RECHAZADA)$", message = "El estado debe ser APROBADA o RECHAZADA")
        String newStatus,

        @NotBlank(message = "La razón es requerida")
        String reason
) {
}