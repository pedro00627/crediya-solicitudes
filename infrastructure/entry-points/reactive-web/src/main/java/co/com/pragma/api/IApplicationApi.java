package co.com.pragma.api;

import co.com.pragma.api.dto.request.ApplicationRequestRecord;
import co.com.pragma.api.exception.dto.ErrorBody;
import co.com.pragma.model.application.Application;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

public interface IApplicationApi {
    @Operation(
            operationId = "saveUser",
            summary = "Crear un nuevo usuario",
            description = "Registra un nuevo usuario en el sistema. Valida la información de entrada y las reglas de negocio.",
            requestBody = @RequestBody(
                    description = "Datos del usuario a crear.",
                    required = true,
                    content = @Content(schema = @Schema(implementation = ApplicationRequestRecord.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Usuario creado exitosamente", content = @Content(schema = @Schema(implementation = Application.class))),
                    @ApiResponse(responseCode = "400", description = "Petición inválida (ej. datos faltantes, formato incorrecto)", content = @Content(schema = @Schema(implementation = ErrorBody.class))),
                    @ApiResponse(responseCode = "409", description = "Conflicto de negocio (ej. usuario ya existe)", content = @Content(schema = @Schema(implementation = ErrorBody.class)))
            }
    )
    Mono<ServerResponse> createLoanApplication(ServerRequest serverRequest);
}
