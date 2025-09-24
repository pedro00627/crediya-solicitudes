package co.com.pragma.r2dbc.helper;

import org.reactivecommons.utils.ObjectMapper;
import org.springframework.data.domain.Example;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.reflect.ParameterizedType;
import java.util.function.Function;

public abstract class ReactiveAdapterOperations<E, D, I, R extends ReactiveCrudRepository<D, I> & ReactiveQueryByExampleExecutor<D>> {
    private final Class<D> dataClass;
    private final Function<D, E> toEntityFn;
    protected R repository;
    protected ObjectMapper mapper;

    @SuppressWarnings("unchecked")
    protected ReactiveAdapterOperations(final R repository, final ObjectMapper mapper, final Function<D, E> toEntityFn) {
        this.repository = repository;
        this.mapper = mapper;
        final ParameterizedType genericSuperclass = (ParameterizedType) getClass().getGenericSuperclass();
        dataClass = (Class<D>) genericSuperclass.getActualTypeArguments()[1];
        this.toEntityFn = toEntityFn;
    }

    protected D toData(final E entity) {
        return this.mapper.map(entity, this.dataClass);
    }

    protected E toEntity(final D data) {
        return null != data ? this.toEntityFn.apply(data) : null;
    }

    public Mono<E> save(final E entity) {
        return this.saveData(this.toData(entity))
                .map(this::toEntity);
    }

    protected Flux<E> saveAllEntities(final Flux<E> entities) {
        return this.saveData(entities.map(this::toData))
                .map(this::toEntity);
    }

    protected Mono<D> saveData(final D data) {
        return this.repository.save(data);
    }

    protected Flux<D> saveData(final Flux<D> data) {
        return this.repository.saveAll(data);
    }

    public Mono<E> findById(final I id) {
        return this.repository.findById(id).map(this::toEntity);
    }

    public Flux<E> findByExample(final E entity) {
        return this.repository.findAll(Example.of(this.toData(entity)))
                .map(this::toEntity);
    }

    public Flux<E> findAll() {
        return this.repository.findAll()
                .map(this::toEntity);
    }
}
