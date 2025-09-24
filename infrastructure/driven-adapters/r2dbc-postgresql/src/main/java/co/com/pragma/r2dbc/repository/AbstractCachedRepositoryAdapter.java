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

    AbstractCachedRepositoryAdapter(final CacheManager cacheManager, final LoggerPort logger) {
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

    public Mono<T> findById(final Object id) {
        final Cache cache = Objects.requireNonNull(this.cacheManager.getCache(this.getCacheName()));

        return Mono.fromCallable(() -> cache.get(id, this.getDomainClass()))
                .doOnSuccess(entity -> {
                    if (null != entity) {
                        this.logger.info("==> CACHE HIT. Obteniendo {} desde la caché con ID: {}", this.getEntityNameForLogging(), id);
                    }
                })
                .switchIfEmpty(this.getFromDatabaseAndCache(id, cache, this::fetchByIdFromDatabase));
    }

    public Mono<T> findByName(final String name) {
        final Cache cache = Objects.requireNonNull(this.cacheManager.getCache(this.getCacheName()));

        return Mono.fromCallable(() -> cache.get(name, this.getDomainClass()))
                .doOnSuccess(entity -> {
                    if (null != entity) {
                        this.logger.info("==> CACHE HIT. Obteniendo {} desde la caché con name: {}", this.getEntityNameForLogging(), name);
                    }
                })
                .switchIfEmpty(this.getFromDatabaseAndCache(name, cache, this::fetchByNameFromDatabase));
    }

    private <K> Mono<T> getFromDatabaseAndCache(final K key, final Cache cache, final Function<K, Mono<T>> databaseFetcher) {
        this.logger.info("==> CACHE MISS. Consultando {} desde la BD con {}: {}", this.getEntityNameForLogging(), this.getKeyTypeForLogging(key), String.valueOf(key));
        return databaseFetcher.apply(key)
                .doOnSuccess(entityFromDb -> {
                    if (null != entityFromDb) {
                        // Cache by the key used for fetching
                        cache.put(key, entityFromDb);
                        // Cache by ID and Name as well, if available
                        final Object entityId = this.getEntityId(entityFromDb);
                        final String entityName = this.getEntityName(entityFromDb);
                        if (null != entityId && !entityId.equals(key)) {
                            cache.put(entityId, entityFromDb);
                        }
                        if (null != entityName && !entityName.equals(key)) {
                            cache.put(entityName, entityFromDb);
                        }
                    }
                });
    }

    private <K> String getKeyTypeForLogging(final K key) {
        if (key instanceof Integer) {
            return "ID";
        }
        if (key instanceof String) {
            return "name";
        }
        return "key";
    }
}
