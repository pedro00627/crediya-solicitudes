package co.com.pragma.model.loantype.gateways;

import co.com.pragma.model.loantype.LoanType;
import reactor.core.publisher.Mono;

public interface LoanTypeGateway {

    /**
     * Busca un tipo de préstamo por su ID. La implementación de este método se utilizará para aplicar la caché.
     */
    Mono<LoanType> findById(Integer id);
}