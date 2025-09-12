package co.com.pragma.r2dbc.repository;

import co.com.pragma.model.application.Application;
import co.com.pragma.model.application.gateways.ApplicationGateway;
import co.com.pragma.model.common.PageRequest;
import co.com.pragma.r2dbc.entity.ApplicationEntity;
import co.com.pragma.r2dbc.helper.ReactiveAdapterOperations;
import co.com.pragma.r2dbc.interfaces.ApplicationReactiveRepository;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Repository
public class ApplicationReactiveGatewayAdapter extends ReactiveAdapterOperations<
        Application,
        ApplicationEntity,
        String,
        ApplicationReactiveRepository
        > implements ApplicationGateway {
    public ApplicationReactiveGatewayAdapter(ApplicationReactiveRepository repository, ObjectMapper mapper) {
        super(repository, mapper, d -> mapper.map(d, Application.class));
    }

    @Override
    public Flux<Application> findOpenApplicationsByDocumentId(String documentId, List<Integer> statusIds) {
        // La lógica ahora se delega completamente a la interfaz del repositorio,
        // manteniendo este adaptador limpio y simple.
        return repository.findOpenApplicationsByDocumentId(documentId, statusIds)
                .map(this::toEntity); // Usa el método 'toEntity' heredado de ReactiveAdapterOperations.
    }

    @Override
    public Flux<Application> findByStatusIn(List<String> statuses, PageRequest pageRequest) {
        Pageable springPageable = org.springframework.data.domain.PageRequest.of(pageRequest.page(), pageRequest.size());
        return repository.findByStatusIn(statuses, springPageable)
                .map(this::toEntity);
    }

    @Override
    public Mono<Long> countByStatusIn(List<String> statuses) {
        return repository.countByStatusIn(statuses);
    }
}
