package co.com.pragma.model.status;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Representa el estado de una solicitud.
 * Convertido de un record a una clase para compatibilidad con librerías
 * que requieren un constructor sin argumentos (ej: Jackson, Spring Data).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Status {
    private int statusId;
    private String name;
    private String description;
}
