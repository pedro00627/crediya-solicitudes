package co.com.pragma.model.application;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Representa la solicitud de préstamo en el dominio.
 * Convertido de un record a una clase para compatibilidad con librerías
 * y frameworks como Mockito.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Application {
    private UUID applicationId;
    private BigDecimal amount;
    private int term;
    private String email;
    private int statusId;
    private int loanTypeId;
}
