package co.com.pragma.api.mapper;

import co.com.pragma.api.exception.InvalidRequestException;
import co.com.pragma.model.loantype.LoanType;
import co.com.pragma.model.loantype.gateways.LoanTypeGateway;
import co.com.pragma.model.status.Status;
import co.com.pragma.model.status.gateways.StatusGateway;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class ApplicationMapperHelper {

    private final StatusGateway statusGateway;
    private final LoanTypeGateway loanTypeGateway;

    public ApplicationMapperHelper(StatusGateway statusGateway, LoanTypeGateway loanTypeGateway) {
        this.statusGateway = statusGateway;
        this.loanTypeGateway = loanTypeGateway;
    }

    public Mono<Integer> getStatusIdByName(String statusName) {
        return statusGateway.findByName(statusName)
                .map(Status::getStatusId)
                .switchIfEmpty(Mono.error(new InvalidRequestException("Status name not found: " + statusName)));
    }

    public Mono<Integer> getLoanTypeIdByName(String loanTypeName) {
        return loanTypeGateway.findByName(loanTypeName)
                .map(LoanType::getLoanTypeId)
                .switchIfEmpty(Mono.error(new InvalidRequestException("Loan type name not found: " + loanTypeName)));
    }

    public Mono<String> getStatusNameById(int statusId) {
        return statusGateway.findById(statusId)
                .map(Status::getName)
                .switchIfEmpty(Mono.error(new InvalidRequestException("Status ID not found: " + statusId)));
    }

    public Mono<String> getLoanTypeNameById(int loanTypeId) {
        return loanTypeGateway.findById(loanTypeId)
                .map(LoanType::getName)
                .switchIfEmpty(Mono.error(new InvalidRequestException("Loan type ID not found: " + loanTypeId)));
    }
}
