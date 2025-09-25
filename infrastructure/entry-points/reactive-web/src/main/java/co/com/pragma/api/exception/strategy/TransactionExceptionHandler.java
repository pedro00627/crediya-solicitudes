package co.com.pragma.api.exception.strategy;

import co.com.pragma.api.exception.dto.ErrorBody;
import co.com.pragma.api.exception.dto.ErrorResponseWrapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Maneja excepciones relacionadas con transacciones de base de datos
 */
@Component
public class TransactionExceptionHandler implements ExceptionHandlerStrategy {

    @Override
    public boolean supports(Class<? extends Throwable> type) {
        return TransactionException.class.isAssignableFrom(type) ||
               DataIntegrityViolationException.class.isAssignableFrom(type) ||
               OptimisticLockingFailureException.class.isAssignableFrom(type);
    }

    @Override
    public Mono<ErrorResponseWrapper> handle(Throwable ex, ServerWebExchange exchange) {
        HttpStatus status;
        String message;
        String error;

        if (ex instanceof DataIntegrityViolationException) {
            // Violación de integridad de datos (constraints, unique, etc.)
            status = HttpStatus.CONFLICT;
            message = "La operación entra en conflicto con los datos existentes";
            error = "Data Integrity Violation";

        } else if (ex instanceof OptimisticLockingFailureException) {
            // Fallo de bloqueo optimista (concurrencia)
            status = HttpStatus.CONFLICT;
            message = "La operación fue modificada por otro usuario. Intente nuevamente";
            error = "Concurrent Modification";

        } else if (ex instanceof TransactionException) {
            // Error general de transacción
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            message = "Error procesando la transacción. Intente nuevamente";
            error = "Transaction Error";

        } else {
            // Otros errores transaccionales
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            message = "Error interno procesando la solicitud";
            error = "Database Transaction Error";
        }

        ErrorBody errorBody = new ErrorBody(
                status.value(),
                error,
                message,
                null
        );

        return Mono.just(new ErrorResponseWrapper(status, errorBody));
    }
}