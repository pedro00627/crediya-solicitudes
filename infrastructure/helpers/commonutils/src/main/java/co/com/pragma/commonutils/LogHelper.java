package co.com.pragma.commonutils;

import java.util.regex.Pattern;

public final class LogHelper {
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9_+&*-]++(?:\\.[a-zA-Z0-9_+&*-]++)*+@(?:[a-zA-Z0-9-]++\\.)++[a-zA-Z]{2,7}$");

    private LogHelper() {
        // Private constructor for utility class
    }

    /**
     * Enmascara una dirección de correo electrónico para logging seguro.
     * Ejemplo: "test.user@pragma.com.co" -> "t***r@pragma.com.co"
     *
     * @param email El correo electrónico a enmascarar.
     * @return El correo electrónico enmascarado.
     */
    public static String maskEmail(String email) {
        if (email == null || !EMAIL_PATTERN.matcher(email).matches()) {
            return "invalid-email-format";
        }
        int atIndex = email.indexOf('@');
        String localPart = email.substring(0, atIndex);
        if (localPart.length() <= 2) {
            return "***" + email.substring(atIndex);
        }
        // Muestra el primer y último caracter de la parte local para mayor seguridad, como indica el comentario.
        return localPart.charAt(0) + "***" + localPart.charAt(localPart.length() - 1) + email.substring(atIndex);
    }
}