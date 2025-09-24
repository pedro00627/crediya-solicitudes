package co.com.pragma.r2dbc.repository;

import co.com.pragma.model.log.gateways.LoggerPort;
import co.com.pragma.model.status.Status;
import co.com.pragma.model.status.gateways.StatusGateway;
import co.com.pragma.r2dbc.interfaces.StatusReactiveRepository;
import co.com.pragma.r2dbc.mapper.StatusMapper;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Set;

@Repository
public class StatusRepositoryAdapter extends AbstractCachedRepositoryAdapter<Status> implements StatusGateway {

    private final StatusReactiveRepository repository;
    private final StatusMapper statusMapper;

    public StatusRepositoryAdapter(final StatusReactiveRepository repository, final StatusMapper statusMapper, final CacheManager cacheManager, final LoggerPort logger) {
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
    protected Object getEntityId(final Status entity) {
        return entity.getStatusId();
    }

    @Override
    protected String getEntityName(final Status entity) {
        return entity.getName();
    }

    @Override
    protected Mono<Status> fetchByIdFromDatabase(final Object id) {
        return this.repository.findById((Integer) id)
                .map(this.statusMapper::toDomain);
    }

    @Override
    protected Mono<Status> fetchByNameFromDatabase(final String name) {
        return this.repository.findByName(name)
                .map(this.statusMapper::toDomain);
    }

    @Override
    public Flux<Status> findAllByIds(final Set<Integer> ids) {
        return this.repository.findAllByStatusIdIn(ids).map(this.statusMapper::toDomain);
    }

    // Implementación explícita de los métodos de StatusGateway
    @Override
    public Mono<Status> findById(final Integer id) {
        return super.findById(id);
    }

    @Override
    public Mono<Status> findByName(final String statusName) {
        return super.findByName(statusName);
    }
}
