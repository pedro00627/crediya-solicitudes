package co.com.pragma.r2dbc.repository;

import co.com.pragma.model.status.Status;
import co.com.pragma.model.status.gateways.StatusGateway;
import co.com.pragma.r2dbc.entity.StatusEntity;
import co.com.pragma.r2dbc.helper.ReactiveAdapterOperations;
import co.com.pragma.r2dbc.interfaces.StatusDataRepository;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public class StatusReactiveGatewayAdapter extends ReactiveAdapterOperations<
        Status,
        StatusEntity,
        Integer,
        StatusDataRepository
        > implements StatusGateway {
    public StatusReactiveGatewayAdapter(StatusDataRepository repository, ObjectMapper mapper) {
        super(repository, mapper, d -> mapper.map(d, Status.class));
    }

    @Override
    public Mono<Status> findById(Integer id) {
        return super.findById(id);
    }
}
