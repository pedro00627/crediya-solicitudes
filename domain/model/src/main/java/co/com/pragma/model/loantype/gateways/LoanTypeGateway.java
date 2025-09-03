package co.com.pragma.model.loantype.gateways;

import co.com.pragma.model.loantype.LoanType;
import reactor.core.publisher.Mono;

public interface LoanTypeGateway {

    /**
     * Busca un tipo de préstamo por su ID. La implementación de este método será la que
     * utilizaremos para aplicar la caché.
     *
     * @param id El ID del tipo de préstamo a buscar.
     * @return Un Mono que emite el tipo de préstamo encontrado o un Mono vacío si no existe.
     */
    Mono<LoanType> findById(Integer id);
}
