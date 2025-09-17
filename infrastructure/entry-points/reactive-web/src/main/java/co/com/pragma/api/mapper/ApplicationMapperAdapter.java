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

    public Mono<ApplicationReviewDTO> toEnrichedReviewDTO(Application application) {
        // Este método se mantiene por si se necesita en otro lugar, pero ya no se usa para la paginación.
        Mono<String> statusNameMono = statusGateway.findById(application.getStatusId()).map(Status::getName).defaultIfEmpty(UNKNOWN_VALUE);
        Mono<LoanType> loanTypeMono = loanTypeGateway.findById(application.getLoanTypeId());

        return Mono.zip(statusNameMono, loanTypeMono)
                .map(tuple -> delegate.toReviewDTO(application, tuple.getT1(), tuple.getT2()))
                .defaultIfEmpty(delegate.toReviewDTO(application, UNKNOWN_VALUE, null));
    }

    public Mono<List<ApplicationReviewDTO>> toEnrichedReviewDTOs(Flux<Application> applications) {
        return applications.collectList().flatMap(appList -> {
            if (appList.isEmpty()) {
                return Mono.just(Collections.emptyList());
            }

            // 1. Recolectar todos los IDs únicos
            Set<Integer> statusIds = appList.stream().map(Application::getStatusId).collect(Collectors.toSet());
            Set<Integer> loanTypeIds = appList.stream().map(Application::getLoanTypeId).collect(Collectors.toSet());

            // 2. Realizar dos consultas en lote (bulk)
            Mono<Map<Integer, String>> statusMapMono = statusGateway.findAllByIds(statusIds)
                    .collectMap(Status::getStatusId, Status::getName); // Asume que el modelo Status tiene getStatusId()
            Mono<Map<Integer, LoanType>> loanTypeMapMono = loanTypeGateway.findAllByIds(loanTypeIds)
                    .collectMap(LoanType::getLoanTypeId, loanType -> loanType); // Asume que el modelo LoanType tiene getLoanTypeId()

            // 3. Combinar los resultados y mapear en memoria
            return Mono.zip(statusMapMono, loanTypeMapMono)
                    .map(tuple -> {
                        Map<Integer, String> statusMap = tuple.getT1();
                        Map<Integer, LoanType> loanTypeMap = tuple.getT2();

                        return appList.stream()
                                .map(app -> {
                                    String statusName = statusMap.getOrDefault(app.getStatusId(), UNKNOWN_VALUE);
                                    LoanType loanType = loanTypeMap.get(app.getLoanTypeId());
                                    return delegate.toReviewDTO(app, statusName, loanType);
                                })
                                .collect(Collectors.toList());
                    });
        });
    }
}