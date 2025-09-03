package co.com.pragma.r2dbc.interfaces;

import co.com.pragma.r2dbc.entity.StatusEntity;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StatusDataRepository extends ReactiveCrudRepository<StatusEntity, Integer>, ReactiveQueryByExampleExecutor<StatusEntity> {
}