package co.com.pragma.api.mapper;

import co.com.pragma.api.dto.request.ApplicationRequestRecord;
import co.com.pragma.api.dto.response.ApplicationResponseRecord;
import co.com.pragma.api.dto.response.LoanTypeResponseRecord;
import co.com.pragma.api.dto.response.ResponseRecord;
import co.com.pragma.api.dto.response.StatusResponseRecord;
import co.com.pragma.model.application.Application;
import co.com.pragma.model.application.ApplicationCreationResult;
import co.com.pragma.model.loantype.LoanType;
import co.com.pragma.model.status.Status;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface IApplicationMapper {

    // --- Mapeo de DTO de Petición a Dominio ---
    Application toModel(ApplicationRequestRecord requestRecord);

    // --- Mapeos de Dominio a DTO de Respuesta ---

    /**
     * Mapeo de soporte: Convierte el resultado completo en el DTO específico de la aplicación.
     * MapStruct es lo suficientemente inteligente como para usar los campos anidados.
     */
    @Mapping(source = "application.applicationId", target = "applicationId")
    @Mapping(source = "application.amount", target = "amount")
    @Mapping(source = "application.term", target = "term")
    @Mapping(source = "application.email", target = "email")
    @Mapping(source = "status.statusId", target = "statusId")
    @Mapping(source = "loanType.loanTypeId", target = "loanTypeId")
    ApplicationResponseRecord toApplicationResponseRecord(ApplicationCreationResult result);

    /**
     * Mapeo de soporte: Convierte el objeto de dominio Status en su DTO.
     */
    StatusResponseRecord toStatusResponseRecord(Status status);

    /**
     * Mapeo de soporte: Convierte el objeto de dominio LoanType en su DTO.
     */
    LoanTypeResponseRecord toLoanTypeResponseRecord(LoanType loanType);

    /**
     * Método principal que orquesta la conversión del resultado del caso de uso
     * al DTO de respuesta completo que se enviará al cliente.
     * Se implementa como un método 'default' para tener control explícito
     * y evitar ambigüedades en la composición de MapStruct.
     */
    default ResponseRecord toResponse(ApplicationCreationResult result) {
        if (result == null) {
            return null;
        }
        ApplicationResponseRecord applicationDTO = toApplicationResponseRecord(result);
        StatusResponseRecord statusDTO = toStatusResponseRecord(result.getStatus());
        LoanTypeResponseRecord loanTypeDTO = toLoanTypeResponseRecord(result.getLoanType());

        return new ResponseRecord(applicationDTO, statusDTO, loanTypeDTO);
    }
}
