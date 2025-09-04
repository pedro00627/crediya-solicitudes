package co.com.pragma.r2dbc.repository;

import co.com.pragma.model.status.Status;
import co.com.pragma.model.status.gateways.StatusGateway;
import co.com.pragma.r2dbc.mapper.StatusMapper;
import co.com.pragma.r2dbc.interfaces.StatusReactiveRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Repository
@RequiredArgsConstructor
@Slf4j
public class StatusRepositoryAdapter implements StatusGateway {

    private final StatusReactiveRepository repository;
    private final StatusMapper statusMapper;
    private final CacheManager cacheManager;

    @Override
    public Mono<Status> findById(Integer id) {
        Cache cache = Objects.requireNonNull(cacheManager.getCache("statuses"));

        // Intenta obtener el valor de la caché de forma reactiva.
        // Si el valor es encontrado (no nulo), se emite y se loguea el CACHE HIT.
        return Mono.fromCallable(() -> cache.get(id, Status.class))
                .doOnSuccess(status -> {
                    if (status != null) {
                        log.info("==> CACHE HIT. Obteniendo ESTADO desde la caché con ID: {}", id);
                    }
                })
                // Si el Mono de la caché está vacío (CACHE MISS), cambia al Mono de la base de datos.
                .switchIfEmpty(getFromDatabaseAndCache(id, cache));
    }

    /**
     * Método privado que define el flujo de "cache miss":
     * 1. Loguea el miss.
     * 2. Consulta la base de datos.
     * 3. Guarda el resultado en la caché como un efecto secundario.
     */
    private Mono<Status> getFromDatabaseAndCache(Integer id, Cache cache) {
        log.info("==> CACHE MISS. Consultando ESTADO desde la BD con ID: {}", id);
        return repository.findById(String.valueOf(id))
                .map(statusMapper::toDomain)
                .doOnSuccess(statusFromDb -> {
                    if (statusFromDb != null) {
                        cache.put(id, statusFromDb);
                    }
                });
    }
}