package co.com.pragma.r2dbc.interfaces;

import co.com.pragma.r2dbc.entity.StatusEntity;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface StatusReactiveRepository extends ReactiveCrudRepository<StatusEntity, String>, ReactiveQueryByExampleExecutor<StatusEntity> {

}
