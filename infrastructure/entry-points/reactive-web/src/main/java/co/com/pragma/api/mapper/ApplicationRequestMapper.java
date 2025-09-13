package co.com.pragma.api.mapper;

import co.com.pragma.api.dto.request.ApplicationRequestRecord;
import co.com.pragma.model.application.Application;
import co.com.pragma.model.application.ApplicationStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class ApplicationRequestMapper implements IApplicationRequestMapper {

    private final IApplicationMapper mapperDelegate;
    private final ApplicationMapperHelper helper;

    @Override
    public Mono<Application> toModel(ApplicationRequestRecord requestRecord) {

        // Crea un Mono para el statusId que maneja la nulidad de forma reactiva.
        Mono<Integer> statusIdMono = Mono.justOrEmpty(requestRecord.statusName())
                .flatMap(helper::getStatusIdByName)
                .defaultIfEmpty(ApplicationStatus.PENDING_REVIEW_ID);

        // Crea un Mono para el loanTypeId, que es obligatorio.
        Mono<Integer> loanTypeIdMono = helper.getLoanTypeIdByName(requestRecord.loanTypeName());

        // Combina ambos Monos. Esto es seguro porque statusIdMono siempre emite un valor.
        return Mono.zip(statusIdMono, loanTypeIdMono)
                .map(tuple -> {
                    Application application = mapperDelegate.toApplication(requestRecord);
                    application.setStatusId(tuple.getT1());
                    application.setLoanTypeId(tuple.getT2());
                    return application;
                });
    }
}
