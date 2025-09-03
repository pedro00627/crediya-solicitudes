package co.com.pragma.api.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record StatusRequestRecord(
        @NotNull(message = "El ID del estado no puede ser nulo")
        @Min(value = 1, message = "El ID del estado debe ser mayor a 0")
        Integer statusId,
        @NotBlank(message = "El nombre no puede estar vacío")
        String name,
        @NotBlank(message = "La descripción no puede estar vacía")
        String description
) {
}
