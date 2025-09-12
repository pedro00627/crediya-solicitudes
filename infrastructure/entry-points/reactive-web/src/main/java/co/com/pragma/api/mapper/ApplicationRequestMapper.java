package co.com.pragma.api.mapper;

import co.com.pragma.api.dto.request.ApplicationRequestRecord;
import co.com.pragma.model.application.Application;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class ApplicationRequestMapper {

    private final IApplicationMapper mapperDelegate;
    private final ApplicationMapperHelper helper;

    public Mono<Application> toModel(ApplicationRequestRecord requestRecord) {
        return Mono.zip(
                        helper.getStatusIdByName(requestRecord.statusName()),
                        helper.getLoanTypeIdByName(requestRecord.loanTypeName())
                )
                .map(tuple -> {
                    Application application = mapperDelegate.toApplication(requestRecord);
                    application.setStatusId(tuple.getT1());
                    application.setLoanTypeId(tuple.getT2());
                    return application;
                });
    }
}