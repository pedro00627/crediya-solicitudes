package co.com.pragma.model.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Representa los datos de un usuario.
 * Convertido de un record a una clase para compatibilidad con librerías
 * que requieren un constructor sin argumentos.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRecord {
    private String id;
    private String firstName;
    private String lastName;
    private LocalDate birthDate;
    private String email;
    private String identityDocument;
    private String phone;
    private Integer roleId;
    private Double baseSalary;
}
