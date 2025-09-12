package co.com.pragma.api.mapper;

import co.com.pragma.api.dto.request.ApplicationRequestRecord;
import co.com.pragma.api.dto.response.ApplicationResponseRecord;
import co.com.pragma.api.dto.response.LoanTypeResponseRecord;
import co.com.pragma.api.dto.response.ResponseRecord;
import co.com.pragma.api.dto.response.StatusResponseRecord;
import co.com.pragma.model.application.Application;
import co.com.pragma.model.application.ApplicationCreationResult;
import co.com.pragma.model.application.ApplicationReviewDTO;
import co.com.pragma.model.loantype.LoanType;
import co.com.pragma.model.status.Status;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface IApplicationMapper {

    // Mapea el resultado enriquecido del caso de uso al DTO de respuesta final.
    @Mapping(source = "application", target = "applicationResponseRecord")
    @Mapping(source = "status", target = "statusResponseRecord")
    @Mapping(target = "loanTypeResponseRecord", expression = "java(toLoanTypeResponseRecord(applicationCreationResult.loanType()))")
    public abstract ResponseRecord toResponse(ApplicationCreationResult applicationCreationResult);

    // Mapea el request DTO al modelo de dominio, ignorando los campos que se generan en el backend.
    @Mapping(target = "applicationId", ignore = true)
    @Mapping(target = "statusId", ignore = true)
    @Mapping(target = "documentId", ignore = true)
    @Mapping(target = "loanTypeId", ignore = true)
    Application toApplication(ApplicationRequestRecord requestRecord);

    @Mapping(source = "application.amount", target = "amount")
    @Mapping(source = "application.term", target = "term")
    @Mapping(source = "application.email", target = "email")
    // Los campos que no están en 'Application' se mapearán desde otros objetos
    @Mapping(source = "loanType.name", target = "loanType")
    @Mapping(source = "loanType.interestRate", target = "interestRate")
    @Mapping(source = "statusName", target = "applicationStatus")
    @Mapping(target = "baseSalary", ignore = true)
    @Mapping(target = "monthlyApplicationAmount", ignore = true)
    ApplicationReviewDTO toReviewDTO(Application application, String statusName, LoanType loanType);


    // Ayuda a MapStruct a mapear el ID del dominio al ID del DTO de respuesta
    public abstract ApplicationResponseRecord toApplicationResponseRecord(Application application);

    // Métodos de mapeo para Status y LoanType a sus DTOs de respuesta
    public abstract StatusResponseRecord toStatusResponseRecord(Status status);

    public abstract LoanTypeResponseRecord toLoanTypeResponseRecord(LoanType loanType);
}
