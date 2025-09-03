package co.com.pragma.model.user;

import java.time.LocalDate;

public record UserRecord(
        String id,
        String firstName,
        String lastName,
        LocalDate birthDate,
        String email,
        String identityDocument,
        String phone,
        Integer roleId,
        Double baseSalary
) {
}