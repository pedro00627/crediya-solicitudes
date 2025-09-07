package co.com.pragma.model.validation;

import co.com.pragma.model.loantype.LoanType;
import co.com.pragma.model.user.UserRecord;

public record ValidationData(LoanType loanType, UserRecord user) {
}