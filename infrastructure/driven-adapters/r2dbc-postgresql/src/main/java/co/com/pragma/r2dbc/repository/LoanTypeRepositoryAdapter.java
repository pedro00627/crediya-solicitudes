package co.com.pragma.r2dbc.repository;

import co.com.pragma.model.loantype.LoanType;
import co.com.pragma.model.loantype.gateways.LoanTypeGateway;
import co.com.pragma.r2dbc.interfaces.LoanTypeDataRepository;
import co.com.pragma.r2dbc.mapper.LoanTypeMapper;
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
public class LoanTypeRepositoryAdapter implements LoanTypeGateway {

    private final LoanTypeDataRepository repository;
    private final LoanTypeMapper loanTypeMapper;
    private final CacheManager cacheManager; // Inyectamos el CacheManager

    @Override
    public Mono<LoanType> findById(Integer id) {
        // Obtenemos la instancia de la caché "loan_types" que configuramos en application.yaml
        Cache cache = Objects.requireNonNull(cacheManager.getCache("loan_types"));

        // Intenta obtener el valor de la caché de forma reactiva.
        // Si el valor es encontrado (no nulo), se emite y se loguea el CACHE HIT.
        return Mono.fromCallable(() -> cache.get(id, LoanType.class))
                .doOnSuccess(loanType -> {
                    if (loanType != null) {
                        log.info("==> CACHE HIT. Obteniendo TIPO DE PRÉSTAMO desde la caché con ID: {}", id);
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
    private Mono<LoanType> getFromDatabaseAndCache(Integer id, Cache cache) {
        log.info("==> CACHE MISS. Consultando TIPO DE PRÉSTAMO desde la BD con ID: {}", id);
        return repository.findById(Integer.valueOf(String.valueOf(id)))
                .map(loanTypeMapper::toDomain)
                .doOnSuccess(loanTypeFromDb -> {
                    if (loanTypeFromDb != null) {
                        cache.put(id, loanTypeFromDb);
                    }
                });
    }
}