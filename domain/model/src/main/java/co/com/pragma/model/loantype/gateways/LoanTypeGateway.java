package co.com.pragma.model.loantype.gateways;

import co.com.pragma.model.loantype.LoanType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Set;

public interface LoanTypeGateway {

    /**
     * Busca un tipo de préstamo por su ID. La implementación de este método se utilizará para aplicar la caché.
     */
    Mono<LoanType> findById(Integer id);

    Mono<LoanType> findByName(String loanTypeName);

    /**
     * Busca todos los tipos de préstamo que coincidan con los IDs proporcionados.
     * @param ids un Set de IDs de tipo de préstamo a buscar.
     * @return un Flux de los tipos de préstamo encontrados.
     */
    Flux<LoanType> findAllByIds(Set<Integer> ids);
}