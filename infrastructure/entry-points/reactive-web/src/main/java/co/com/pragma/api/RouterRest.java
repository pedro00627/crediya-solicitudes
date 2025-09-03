package co.com.pragma.api;

import co.com.pragma.api.dto.request.ApplicationRequestRecord;
import co.com.pragma.api.dto.response.ResponseRecord;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springdoc.core.annotations.RouterOperation;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class RouterRest {
    @Bean
    @RouterOperation(
            path = "/api/v1/solicitud",
            operation = @Operation(
                    operationId = "createLoanApplication",
                    summary = "Crear una nueva solicitud de préstamo",
                    tags = {"Loan Applications"},
                    requestBody = @RequestBody(
                            required = true,
                            description = "Datos para la nueva solicitud de préstamo",
                            content = @Content(schema = @Schema(implementation = ApplicationRequestRecord.class))
                    ),
                    responses = {
                            @ApiResponse(responseCode = "201", description = "Solicitud creada exitosamente", content = @Content(schema = @Schema(implementation = ResponseRecord.class))),
                            @ApiResponse(responseCode = "400", description = "Solicitud inválida (datos faltantes o incorrectos)"),
                            @ApiResponse(responseCode = "409", description = "Conflicto de negocio (ej: el usuario no es cliente, el monto está fuera de rango)")
                    }
            )
    )
    public RouterFunction<ServerResponse> routerFunction(Handler handler) {
        return route(POST("/api/v1/solicitud").and(accept(MediaType.APPLICATION_JSON)), handler::createLoanApplication);
    }
}