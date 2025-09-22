package co.com.pragma.model.exception;

/**
 * Centraliza los mensajes de error de negocio para mantener la consistencia
 * y facilitar el mantenimiento.
 */
public enum BusinessMessages {
    ;

    public static final String LOAN_TYPE_NOT_FOUND = "El tipo de préstamo especificado no existe.";
    public static final String USER_NOT_FOUND = "El usuario con el email especificado no existe.";
    public static final String INITIAL_STATUS_NOT_FOUND = "El estado inicial 'Pendiente' no está configurado en el sistema.";
    public static final String USER_HAS_ACTIVE_APPLICATION = "El usuario (identificado por email y/o documento) ya tiene una solicitud de préstamo activa.";

}