package co.com.pragma.r2dbc.repository;

import co.com.pragma.model.log.gateways.LoggerPort;
import co.com.pragma.model.status.Status;
import co.com.pragma.model.status.gateways.StatusGateway;
import co.com.pragma.r2dbc.interfaces.StatusReactiveRepository;
import co.com.pragma.r2dbc.mapper.StatusMapper;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public class StatusRepositoryAdapter extends AbstractCachedRepositoryAdapter<Status> implements StatusGateway {

    private final StatusReactiveRepository repository;
    private final StatusMapper statusMapper;

    public StatusRepositoryAdapter(StatusReactiveRepository repository, StatusMapper statusMapper, CacheManager cacheManager, LoggerPort logger) {
        super(cacheManager, logger);
        this.repository = repository;
        this.statusMapper = statusMapper;
    }

    @Override
    protected String getCacheName() {
        return "statuses";
    }

    @Override
    protected String getEntityNameForLogging() {
        return "ESTADO";
    }

    @Override
    protected Class<Status> getDomainClass() {
        return Status.class;
    }

    @Override
    protected Object getEntityId(Status entity) {
        return entity.getStatusId();
    }

    @Override
    protected String getEntityName(Status entity) {
        return entity.getName();
    }

    @Override
    protected Mono<Status> fetchByIdFromDatabase(Object id) {
        // Asumiendo que el ID de Status es String en la base de datos
        return repository.findById(String.valueOf(id))
                .map(statusMapper::toDomain);
    }

    @Override
    protected Mono<Status> fetchByNameFromDatabase(String name) {
        return repository.findByName(name)
                .map(statusMapper::toDomain);
    }

    // Implementación explícita de los métodos de StatusGateway
    @Override
    public Mono<Status> findById(Integer id) {
        return super.findById(id);
    }

    @Override
    public Mono<Status> findByName(String statusName) {
        return super.findByName(statusName);
    }
}
