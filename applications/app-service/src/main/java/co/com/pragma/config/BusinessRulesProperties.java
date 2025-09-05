package co.com.pragma.config;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Mapea las propiedades de configuración relacionadas con las reglas de negocio
 * desde el archivo application.yaml (bajo el prefijo 'app.rules').
 * Esto permite una gestión centralizada y tipada de las constantes de negocio.
 */
@ConfigurationProperties(prefix = "app.rules")
@Data
@Validated
public class BusinessRulesProperties {

    @NotNull(message = "El ID del rol del cliente no puede ser nulo")
    private Integer clientRoleId;

    @NotNull(message = "El ID del estado pendiente no puede ser nulo")
    private Integer pendingStatusId;

}
