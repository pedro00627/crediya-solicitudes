package co.com.pragma.api.dto;

import co.com.pragma.model.application.Application;
import co.com.pragma.model.loantype.LoanType;
import co.com.pragma.model.status.Status;
import co.com.pragma.model.user.UserRecord;

public record ApplicationCreationResult(
        Application application,
        LoanType loanType,
        Status status,
        UserRecord user
) {
}