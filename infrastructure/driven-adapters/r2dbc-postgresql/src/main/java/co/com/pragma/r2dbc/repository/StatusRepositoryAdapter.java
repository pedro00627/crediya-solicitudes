package co.com.pragma.r2dbc.repository;

import co.com.pragma.model.status.Status;
import co.com.pragma.model.status.gateways.StatusGateway;
import co.com.pragma.r2dbc.interfaces.StatusDataRepository;
import co.com.pragma.r2dbc.mapper.StatusMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
@Repository // Usamos @Repository para ser consistentes con ApplicationReactiveRepositoryAdapter
@RequiredArgsConstructor
public class StatusRepositoryAdapter implements StatusGateway {

    private final StatusDataRepository repository; // Tu repositorio R2DBC
    private final StatusMapper statusMapper;

    @Override
    public Mono<Status> findById(Integer id) {
        // Envolvemos la llamada al método cacheado en un flujo reactivo
        // que se ejecuta en un hilo separado para no bloquear el event-loop.
        return Mono.fromCallable(() -> findByIdAndCache(id))
                .subscribeOn(Schedulers.boundedElastic())
                // Si el método devuelve null (no encontrado), lo convertimos en un Mono vacío.
                .flatMap(Mono::justOrEmpty);
    }

    /**
     * Este es el método que realmente hace el trabajo y se beneficia de la caché.
     * Es bloqueante (por el .block()), por eso lo aislamos del flujo reactivo principal.
     */
    @Cacheable(value = "statuses", key = "#id")
    public Status findByIdAndCache(Integer id) {
        // Este log es la prueba clave: SOLO se imprimirá la primera vez que se llame
        // con un ID específico. Las siguientes veces, el resultado vendrá de la caché.
        log.info("==> CACHE MISS. Consultando ESTADO desde la BD con ID: {}", id);

        return repository.findById(id)
                .map(statusMapper::toDomain) // Mapeamos la entidad de BD al modelo de dominio
                .block(); // Obtenemos el resultado de forma síncrona para la caché.
    }
}