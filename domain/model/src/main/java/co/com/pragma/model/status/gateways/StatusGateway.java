package co.com.pragma.model.status.gateways;

import co.com.pragma.model.status.Status;
import reactor.core.publisher.Mono;

public interface StatusGateway {

    /**
     * Busca un estado por su ID. La implementación de este método será la que
     * utilizaremos para aplicar la caché.
     *
     * @param id El ID del estado a buscar.
     * @return Un Mono que emite el estado encontrado o un Mono vacío si no existe.
     */
    Mono<Status> findById(Integer id);
}