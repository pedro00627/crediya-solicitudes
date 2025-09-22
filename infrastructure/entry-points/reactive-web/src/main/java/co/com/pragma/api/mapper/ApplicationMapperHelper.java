package co.com.pragma.api.mapper;

import co.com.pragma.api.exception.InvalidRequestException;
import co.com.pragma.model.loantype.LoanType;
import co.com.pragma.model.loantype.gateways.LoanTypeGateway;
import co.com.pragma.model.status.Status;
import co.com.pragma.model.status.gateways.StatusGateway;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Component
public class ApplicationMapperHelper {

    private final StatusGateway statusGateway;
    private final LoanTypeGateway loanTypeGateway;

    public ApplicationMapperHelper(final StatusGateway statusGateway, final LoanTypeGateway loanTypeGateway) {
        this.statusGateway = statusGateway;
        this.loanTypeGateway = loanTypeGateway;
    }

    public Mono<Integer> getStatusIdByName(final String statusName) {
        // La validación de nulidad se elimina. La lógica condicional estará en el mapper principal.
        return this.statusGateway.findByName(statusName)
                .map(Status::getStatusId)
                .switchIfEmpty(Mono.error(new InvalidRequestException("Nombre de estado no encontrado: " + statusName)));
    }

    public Mono<Integer> getLoanTypeIdByName(final String loanTypeName) {
        if (Objects.isNull(loanTypeName)) {
            return Mono.error(new InvalidRequestException("El campo 'loanTypeName' no puede ser nulo."));
        }
        return this.loanTypeGateway.findByName(loanTypeName)
                .map(LoanType::getLoanTypeId)
                .switchIfEmpty(Mono.error(new InvalidRequestException("Nombre de tipo de préstamo no encontrado: " + loanTypeName)));
    }

    public Mono<String> getStatusNameById(final int statusId) {
        return this.statusGateway.findById(statusId)
                .map(Status::getName)
                .switchIfEmpty(Mono.error(new InvalidRequestException("ID de estado no encontrado: " + statusId)));
    }

    public Mono<String> getLoanTypeNameById(final int loanTypeId) {
        return this.loanTypeGateway.findById(loanTypeId)
                .map(LoanType::getName)
                .switchIfEmpty(Mono.error(new InvalidRequestException("ID de tipo de préstamo no encontrado: " + loanTypeId)));
    }
}
