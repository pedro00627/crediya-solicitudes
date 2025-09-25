package co.com.pragma.api.dto.request;

import co.com.pragma.model.constants.ValidationMessages;
import jakarta.validation.constraints.NotBlank;

public record ApproveApplicationRequestRecord(
        @NotBlank(message = ValidationMessages.ADVISOR_ID_REQUIRED)
        String advisorId,

        @NotBlank(message = ValidationMessages.APPROVAL_NOTES_REQUIRED)
        String approvalNotes
) {
}