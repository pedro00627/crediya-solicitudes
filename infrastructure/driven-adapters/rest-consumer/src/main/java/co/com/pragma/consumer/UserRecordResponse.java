package co.com.pragma.consumer;

import java.time.LocalDate;

public record UserRecordResponse(
        String id,
        String firstName,
        String lastName,
        LocalDate birthDate,
        String email,
        String identityDocument,
        String phone,
        String roleId,
        Double baseSalary
) {
}