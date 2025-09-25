package co.com.pragma.model.constants;

/**
 * Mensajes de validación específicos para el módulo de Solicitudes.
 * Centraliza todos los mensajes de error relacionados con la validación de solicitudes de préstamo.
 */
public enum ValidationMessages {
    ;

    // Role Validation Messages
    public static final String INVALID_USER_ROLE = "El usuario no tiene el rol requerido para esta operación.";

    // Loan Amount Validation Messages
    public static final String LOAN_AMOUNT_OUT_OF_RANGE = "El monto solicitado está fuera de los límites para el tipo de préstamo seleccionado.";

    // Application Validation Messages
    public static final String APPLICATION_NOT_FOUND = "La solicitud no existe";
    public static final String USER_HAS_ACTIVE_APPLICATION = "El usuario ya tiene una solicitud de préstamo activa";
    public static final String INVALID_APPLICATION_STATUS = "Estado de solicitud inválido";
    public static final String APPLICATION_CANNOT_BE_MODIFIED = "La solicitud no puede ser modificada en su estado actual";

    // Approval Validation Messages
    public static final String ADVISOR_ID_REQUIRED = "El ID del asesor es requerido";
    public static final String APPROVAL_NOTES_REQUIRED = "Las notas de aprobación son requeridas";
    public static final String APPROVED_AMOUNT_REQUIRED = "El monto aprobado es requerido";
    public static final String APPROVED_AMOUNT_POSITIVE = "El monto aprobado debe ser positivo";
    public static final String APPROVED_TERMS_REQUIRED = "El plazo aprobado es requerido";
    public static final String APPROVED_TERMS_POSITIVE = "El plazo aprobado debe ser positivo";
    public static final String APPLICATION_NOT_PENDING = "Solo se pueden evaluar solicitudes en estado pendiente";

    // Rejection Validation Messages
    public static final String REJECTION_REASON_REQUIRED = "El motivo del rechazo es requerido";
    public static final String REJECTION_NOTES_REQUIRED = "Las notas de rechazo son requeridas";

    // Loan Type Validation Messages
    public static final String LOAN_TYPE_NOT_FOUND = "El tipo de préstamo especificado no existe";
    public static final String INVALID_LOAN_AMOUNT = "El monto del préstamo no es válido";
    public static final String INVALID_LOAN_TERM = "El plazo del préstamo no es válido";

    // Business Rule Constants
    public static final Integer CLIENT_ROLE_ID = 3;
    public static final Integer ADVISOR_ROLE_ID = 2;
    public static final Integer ADMIN_ROLE_ID = 1;

}