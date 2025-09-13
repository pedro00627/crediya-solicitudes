package co.com.pragma.r2dbc.repository;

import co.com.pragma.model.log.gateways.LoggerPort;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.function.Function;

public abstract class AbstractCachedRepositoryAdapter<T> {

    protected final CacheManager cacheManager;
    protected final LoggerPort logger;

    AbstractCachedRepositoryAdapter(CacheManager cacheManager, LoggerPort logger) {
        this.cacheManager = cacheManager;
        this.logger = logger;
    }

    protected abstract String getCacheName();

    protected abstract String getEntityNameForLogging(); // For logging purposes (e.g., "TIPO DE PRÉSTAMO", "ESTADO")

    protected abstract Class<T> getDomainClass(); // To get the Class<T> for cache.get()

    protected abstract Object getEntityId(T entity); // New: Get ID from domain object

    protected abstract String getEntityName(T entity); // New: Get name from domain object

    // Abstract methods for concrete adapters to implement their specific database fetching logic
    protected abstract Mono<T> fetchByIdFromDatabase(Object id);

    protected abstract Mono<T> fetchByNameFromDatabase(String name);

    public Mono<T> findById(Object id) {
        Cache cache = Objects.requireNonNull(cacheManager.getCache(getCacheName()));

        return Mono.fromCallable(() -> cache.get(id, getDomainClass()))
                .doOnSuccess(entity -> {
                    if (entity != null) {
                        logger.info("==> CACHE HIT. Obteniendo {} desde la caché con ID: {}", getEntityNameForLogging(), id);
                    }
                })
                .switchIfEmpty(getFromDatabaseAndCache(id, cache, this::fetchByIdFromDatabase));
    }

    public Mono<T> findByName(String name) {
        Cache cache = Objects.requireNonNull(cacheManager.getCache(getCacheName()));

        return Mono.fromCallable(() -> cache.get(name, getDomainClass()))
                .doOnSuccess(entity -> {
                    if (entity != null) {
                        logger.info("==> CACHE HIT. Obteniendo {} desde la caché con name: {}", getEntityNameForLogging(), name);
                    }
                })
                .switchIfEmpty(getFromDatabaseAndCache(name, cache, this::fetchByNameFromDatabase));
    }

    private <K> Mono<T> getFromDatabaseAndCache(K key, Cache cache, Function<K, Mono<T>> databaseFetcher) {
        logger.info("==> CACHE MISS. Consultando {} desde la BD con {}: {}", getEntityNameForLogging(), getKeyTypeForLogging(key), String.valueOf(key));
        return databaseFetcher.apply(key)
                .doOnSuccess(entityFromDb -> {
                    if (entityFromDb != null) {
                        // Cache by the key used for fetching
                        cache.put(key, entityFromDb);
                        // Cache by ID and Name as well, if available
                        Object entityId = getEntityId(entityFromDb);
                        String entityName = getEntityName(entityFromDb);
                        if (entityId != null && !entityId.equals(key)) {
                            cache.put(entityId, entityFromDb);
                        }
                        if (entityName != null && !entityName.equals(key)) {
                            cache.put(entityName, entityFromDb);
                        }
                    }
                });
    }

    private <K> String getKeyTypeForLogging(K key) {
        if (key instanceof Integer) {
            return "ID";
        }
        if (key instanceof String) {
            return "name";
        }
        return "key";
    }
}
