package co.com.pragma.api.mapper;

import co.com.pragma.api.dto.request.ApplicationRequestRecord;
import co.com.pragma.api.dto.response.ApplicationResponseRecord;
import co.com.pragma.api.dto.response.ResponseRecord;
import co.com.pragma.api.dto.response.StatusResponseRecord;
import co.com.pragma.api.dto.response.LoanTypeResponseRecord;
import co.com.pragma.model.application.Application;
import co.com.pragma.model.application.ApplicationCreationResult;
import co.com.pragma.model.status.Status;
import co.com.pragma.model.loantype.LoanType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Mono;

@Mapper(componentModel = "spring")
public abstract class IApplicationMapper {

    @Autowired
    protected ApplicationMapperHelper helper;

    // Mapea el resultado enriquecido del caso de uso al DTO de respuesta final.
    @Mapping(source = "application", target = "applicationResponseRecord")
    @Mapping(source = "status", target = "statusResponseRecord")
    @Mapping(source = "loanType", target = "loanTypeResponseRecord")
    public abstract ResponseRecord toResponse(ApplicationCreationResult applicationCreationResult);

    // Mapea el request DTO al modelo de dominio, ignorando los campos que se generan en el backend.
    @Mapping(target = "applicationId", ignore = true)
    @Mapping(target = "statusId", ignore = true)
    @Mapping(target = "documentId", ignore = true)
    @Mapping(target = "loanTypeId", ignore = true)
    protected abstract Application toApplication(ApplicationRequestRecord requestRecord);

    public Mono<Application> toModel(ApplicationRequestRecord requestRecord) {
        return Mono.zip(helper.getStatusIdByName(requestRecord.statusName()),
                        helper.getLoanTypeIdByName(requestRecord.loanTypeName()))
                .map(tuple -> {
                    Application application = toApplication(requestRecord);
                    application.setStatusId(tuple.getT1());
                    application.setLoanTypeId(tuple.getT2());
                    return application;
                });
    }

    // Ayuda a MapStruct a mapear el ID del dominio al ID del DTO de respuesta
    public abstract ApplicationResponseRecord toApplicationResponseRecord(Application application);

    // Métodos de mapeo para Status y LoanType a sus DTOs de respuesta
    public abstract StatusResponseRecord toStatusResponseRecord(Status status);
    public abstract LoanTypeResponseRecord toLoanTypeResponseRecord(LoanType loanType);
}
