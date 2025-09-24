package co.com.pragma.model.exception;

public class BusinessException extends RuntimeException {
    public BusinessException(final String message) {
        super(message);
    }
}