package co.com.pragma.r2dbc.interfaces;

import co.com.pragma.r2dbc.entity.StatusEntity;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;

public interface StatusReactiveRepository extends ReactiveCrudRepository<StatusEntity, Integer>, ReactiveQueryByExampleExecutor<StatusEntity> {
    Mono<StatusEntity> findByName(String name);

    Flux<StatusEntity> findAllByStatusIdIn(Collection<Integer> ids);
}
