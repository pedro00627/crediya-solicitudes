package co.com.pragma.api;

import co.com.pragma.api.dto.request.ApplicationRequestRecord;
import co.com.pragma.api.dto.request.UpdateApplicationStatusRequestRecord;
import co.com.pragma.api.dto.response.ResponseRecord;
import co.com.pragma.api.dto.response.ApplicationStatusUpdateResponseRecord;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

public interface IApplicationCommandApi {
    @Operation(operationId = "createLoanApplication", summary = "Crear una nueva solicitud de préstamo", tags = "Loan Applications", requestBody = @RequestBody(required = true, description = "Datos para la nueva solicitud de préstamo", content = @Content(schema = @Schema(implementation = ApplicationRequestRecord.class))), responses = {
            @ApiResponse(responseCode = "201", description = "Solicitud creada exitosamente", content = @Content(schema = @Schema(implementation = ResponseRecord.class))),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida (datos faltantes o incorrectos)"),
            @ApiResponse(responseCode = "409", description = "Conflicto de negocio (ej: el usuario no es cliente, el monto está fuera de rango)")
    })
    Mono<ServerResponse> createLoanApplication(ServerRequest serverRequest);

    @Operation(operationId = "updateApplicationStatus", summary = "Aprobar o rechazar una solicitud de préstamo", tags = "Loan Applications", requestBody = @RequestBody(required = true, description = "Datos para actualizar el estado de la solicitud", content = @Content(schema = @Schema(implementation = UpdateApplicationStatusRequestRecord.class))), responses = {
            @ApiResponse(responseCode = "200", description = "Estado actualizado exitosamente", content = @Content(schema = @Schema(implementation = ApplicationStatusUpdateResponseRecord.class))),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida (ID inválido o estado no permitido)"),
            @ApiResponse(responseCode = "404", description = "Solicitud no encontrada"),
            @ApiResponse(responseCode = "409", description = "Conflicto de negocio (la solicitud no está en estado pendiente)")
    })
    Mono<ServerResponse> updateApplicationStatus(ServerRequest serverRequest);
}