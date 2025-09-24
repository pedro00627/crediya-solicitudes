package co.com.pragma.api.exception;

import jakarta.validation.ConstraintViolation;
import lombok.Getter;

import java.io.Serial;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
public class InvalidRequestException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    private final transient Set<? extends ConstraintViolation<?>> violations;

    public InvalidRequestException(final Set<? extends ConstraintViolation<?>> violations) {
        super(InvalidRequestException.buildMessage(violations));
        this.violations = violations;
    }

    // Nuevo constructor para manejar mensajes de String
    public InvalidRequestException(final String message) {
        super(message);
        violations = Collections.emptySet(); // No hay violaciones específicas, se usa un conjunto vacío
    }

    private static String buildMessage(final Set<? extends ConstraintViolation<?>> violations) {
        return violations.stream()
                .map(v -> String.format("'%s': %s", v.getPropertyPath(), v.getMessage()))
                .collect(Collectors.joining(", "));
    }
}
