package co.com.pragma.api.mapper;

import co.com.pragma.api.dto.request.ApplicationRequestRecord;
import co.com.pragma.api.dto.response.ApplicationResponseRecord;
import co.com.pragma.api.dto.response.ResponseRecord;
import co.com.pragma.model.application.Application;
import co.com.pragma.model.application.ApplicationCreationResult;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface IApplicationMapper {

    // Mapea la respuesta enriquecida del caso de uso al DTO de respuesta final
    @Mapping(source = "application", target = "applicationResponseRecord")
    @Mapping(source = "status", target = "statusResponseRecord")
    @Mapping(source = "loanType", target = "loanTypeResponseRecord")
    ResponseRecord toResponse(ApplicationCreationResult applicationCreationResult);

    // Mapea el request DTO al modelo de dominio, ignorando los campos que se generan en el backend.
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "statusId", ignore = true)
    Application toModel(ApplicationRequestRecord requestRecord);

    // Ayuda a MapStruct a mapear el ID del dominio al ID del DTO de respuesta
    @Mapping(source = "id", target = "applicationId")
    ApplicationResponseRecord toApplicationResponseRecord(Application application);
}
