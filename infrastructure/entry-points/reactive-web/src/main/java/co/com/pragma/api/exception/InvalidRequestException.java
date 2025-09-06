package co.com.pragma.api.exception;

import jakarta.validation.ConstraintViolation;
import lombok.Getter;

import java.io.Serial;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
public class InvalidRequestException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    // Se marca como 'transient' para que la serialización de Java ignore este campo,
    // ya que ConstraintViolation no es necesariamente serializable.
    private final transient Set<? extends ConstraintViolation<?>> violations;

    public InvalidRequestException(Set<? extends ConstraintViolation<?>> violations) {
        super(buildMessage(violations));
        this.violations = violations;
    }

    private static String buildMessage(Set<? extends ConstraintViolation<?>> violations) {
        return violations.stream()
                .map(v -> String.format("'%s': %s", v.getPropertyPath(), v.getMessage()))
                .collect(Collectors.joining(", "));
    }
}