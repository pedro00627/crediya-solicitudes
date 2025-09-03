package co.com.pragma.api.exception;

import jakarta.validation.ConstraintViolation;
import lombok.Getter;

import java.util.Set;
import java.util.stream.Collectors;

@Getter
public class InvalidRequestException extends RuntimeException {

    private final Set<? extends ConstraintViolation<?>> violations;

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