package co.com.pragma.api.mapper;

import co.com.pragma.api.dto.ApplicationReviewDTO;
import co.com.pragma.model.application.Application;
import co.com.pragma.model.loantype.LoanType;
import co.com.pragma.model.loantype.gateways.LoanTypeGateway;
import co.com.pragma.model.status.Status;
import co.com.pragma.model.status.gateways.StatusGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ApplicationMapperAdapter {

    private static final String UNKNOWN_VALUE = "Desconocido";

    private final IApplicationMapper delegate;
    private final StatusGateway statusGateway;
    private final LoanTypeGateway loanTypeGateway;

    public Mono<ApplicationReviewDTO> toEnrichedReviewDTO(final Application application) {
        // Este método se mantiene por si se necesita en otro lugar, pero ya no se usa para la paginación.
        final Mono<String> statusNameMono = this.statusGateway.findById(application.getStatusId()).map(Status::getName).defaultIfEmpty(ApplicationMapperAdapter.UNKNOWN_VALUE);
        final Mono<LoanType> loanTypeMono = this.loanTypeGateway.findById(application.getLoanTypeId());

        return Mono.zip(statusNameMono, loanTypeMono)
                .map(tuple -> this.delegate.toReviewDTO(application, tuple.getT1(), tuple.getT2()))
                .defaultIfEmpty(this.delegate.toReviewDTO(application, ApplicationMapperAdapter.UNKNOWN_VALUE, null));
    }

    public Mono<List<ApplicationReviewDTO>> toEnrichedReviewDTOs(final Flux<Application> applications) {
        return applications.collectList().flatMap(appList -> {
            if (appList.isEmpty()) {
                return Mono.just(Collections.emptyList());
            }

            // 1. Recolectar todos los IDs únicos
            final Set<Integer> statusIds = appList.stream().map(Application::getStatusId).collect(Collectors.toSet());
            final Set<Integer> loanTypeIds = appList.stream().map(Application::getLoanTypeId).collect(Collectors.toSet());

            // 2. Realizar dos consultas en lote (bulk)
            final Mono<Map<Integer, String>> statusMapMono = this.statusGateway.findAllByIds(statusIds)
                    .collectMap(Status::getStatusId, Status::getName); // Asume que el modelo Status tiene getStatusId()
            final Mono<Map<Integer, LoanType>> loanTypeMapMono = this.loanTypeGateway.findAllByIds(loanTypeIds)
                    .collectMap(LoanType::getLoanTypeId, loanType -> loanType); // Asume que el modelo LoanType tiene getLoanTypeId()

            // 3. Combinar los resultados y mapear en memoria
            return Mono.zip(statusMapMono, loanTypeMapMono)
                    .map(tuple -> {
                        final Map<Integer, String> statusMap = tuple.getT1();
                        final Map<Integer, LoanType> loanTypeMap = tuple.getT2();

                        return appList.stream()
                                .map(app -> {
                                    final String statusName = statusMap.getOrDefault(app.getStatusId(), ApplicationMapperAdapter.UNKNOWN_VALUE);
                                    final LoanType loanType = loanTypeMap.get(app.getLoanTypeId());
                                    return this.delegate.toReviewDTO(app, statusName, loanType);
                                })
                                .collect(Collectors.toList());
                    });
        });
    }
}