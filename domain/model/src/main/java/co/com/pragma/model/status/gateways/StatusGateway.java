package co.com.pragma.model.status.gateways;

import co.com.pragma.model.status.Status;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Set;

public interface StatusGateway {

    /**
     * Busca un estado por su ID. La implementación de este método se utilizará para aplicar la caché.
     */
    Mono<Status> findById(Integer id);

    /**
     * @param statusName
     * @return
     */
    Mono<Status> findByName(String statusName);

    /**
     * Busca todos los estados que coincidan con los IDs proporcionados.
     *
     * @param ids un Set de IDs de estado a buscar.
     * @return un Flux de los estados encontrados.
     */
    Flux<Status> findAllByIds(Set<Integer> ids);
}