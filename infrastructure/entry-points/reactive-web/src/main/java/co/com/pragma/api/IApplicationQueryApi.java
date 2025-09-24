package co.com.pragma.api;

import co.com.pragma.api.dto.response.PagedResponseDTO;
import co.com.pragma.api.exception.dto.ErrorBody;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

public interface IApplicationQueryApi {
    @Operation(operationId = "getApplicationsForReview", summary = "Obtener listado de solicitudes para revisión manual", tags = "Loan Applications", parameters = {
            @Parameter(name = "page", in = ParameterIn.QUERY, description = "Número de la página a consultar (inicia en 0)", schema = @Schema(type = "integer", defaultValue = "0")),
            @Parameter(name = "size", in = ParameterIn.QUERY, description = "Cantidad de elementos por página", schema = @Schema(type = "integer", defaultValue = "10"))
    }, responses = {
            @ApiResponse(responseCode = "200", description = "Listado de solicitudes obtenido exitosamente", content = @Content(schema = @Schema(implementation = PagedResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Parámetros de paginación inválidos", content = @Content(schema = @Schema(implementation = ErrorBody.class))),
            @ApiResponse(responseCode = "403", description = "Acceso denegado. Se requiere rol 'Asesor'")
    })
    Mono<ServerResponse> getApplicationsForReview(ServerRequest serverRequest);
}