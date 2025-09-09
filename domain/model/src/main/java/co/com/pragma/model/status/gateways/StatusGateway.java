package co.com.pragma.model.status.gateways;

import co.com.pragma.model.status.Status;
import reactor.core.publisher.Mono;

public interface StatusGateway {

    /**
     * Busca un estado por su ID. La implementación de este método se utilizará para aplicar la caché.
     */
    Mono<Status> findById(Integer id);

    Mono<Status> findByName(String statusName);
}