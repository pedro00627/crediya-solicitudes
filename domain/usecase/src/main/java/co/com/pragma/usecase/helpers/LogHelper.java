package co.com.pragma.usecase.helpers;

public final class LogHelper {

    private LogHelper() {
        // Clase de utilidad, no debe ser instanciada
    }

    /**
     * Enmascara una dirección de correo electrónico para logging seguro.
     * Ejemplo: "test.user@pragma.com.co" -> "t***r@pragma.com.co"
     *
     * @param email El correo electrónico a enmascarar.
     * @return El correo electrónico enmascarado.
     */
    public static String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "invalid-email";
        }
        int atIndex = email.indexOf('@');
        if (atIndex <= 1) {
            return "***" + email.substring(atIndex);
        }
        return email.charAt(0) + "***" + email.charAt(atIndex - 1) + email.substring(atIndex);
    }
}