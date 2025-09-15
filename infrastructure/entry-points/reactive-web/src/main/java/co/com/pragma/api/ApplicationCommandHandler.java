package co.com.pragma.api;

import co.com.pragma.api.dto.request.ApplicationRequestRecord;
import co.com.pragma.api.exception.InvalidRequestException;
import co.com.pragma.api.mapper.IApplicationRequestMapper;
import co.com.pragma.api.mapper.IApplicationResponseHandler;
import co.com.pragma.usecase.application.CreateLoanApplicationUseCase;
import co.com.pragma.model.log.gateways.LoggerPort;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.security.Principal;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ApplicationCommandHandler implements IApplicationCommandApi {

    private final IApplicationRequestMapper requestMapper;
    private final CreateLoanApplicationUseCase useCase;
    private final IApplicationResponseHandler responseHandler;
    private final Validator validator;
    private final LoggerPort logger;

    @Override
    public Mono<ServerResponse> createLoanApplication(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(ApplicationRequestRecord.class)
                .zipWith(serverRequest.principal())
                .flatMap(tuple -> validateTokenEmail(tuple.getT1(), tuple.getT2()))
                .flatMap(this::validateRequest)
                .flatMap(requestMapper::toModel)
                .flatMap(useCase::createLoanApplication) // 1. Se llama al caso de uso
                .switchIfEmpty(Mono.defer(() -> { // 2. Se maneja el caso en que el Mono del caso de uso esté vacío
                    String errorMessage = "El caso de uso no produjo ningún resultado (Mono vacío).";
                    logger.error(errorMessage, new IllegalStateException(errorMessage));
                    return Mono.error(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno al procesar la solicitud."));
                }))
                .doOnNext(result -> // 3. Efecto secundario (log) solo si hay un resultado exitoso
                        logger.info("Proceso de creación de solicitud finalizado exitosamente para el ID: {}", result.application().getApplicationId())
                )
                .flatMap(responseHandler::buildCreationResponse);
    }

    private Mono<ApplicationRequestRecord> validateTokenEmail(ApplicationRequestRecord request, Principal principal) {
        String tokenEmail = principal.getName();
        if (!tokenEmail.equals(request.email())) {
            logger.warn("El email del token ({}) no coincide con el email de la solicitud ({})", logger.maskEmail(tokenEmail), logger.maskEmail(request.email()));
            return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN, "El correo electrónico de la solicitud no coincide con el del token."));
        }
        return Mono.just(request);
    }

    private Mono<ApplicationRequestRecord> validateRequest(ApplicationRequestRecord request) {
        logger.info("Validando petición para crear solicitud para el email: {}", logger.maskEmail(request.email()));
        Set<ConstraintViolation<ApplicationRequestRecord>> violations = validator.validate(request);
        if (violations.isEmpty()) {
            return Mono.just(request);
        }
        String errors = violations.stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .collect(Collectors.joining(", "));
        logger.warn("La solicitud para {} contiene datos inválidos. Violaciones: {}", logger.maskEmail(request.email()), errors);
        return Mono.error(new InvalidRequestException(violations));
    }
}
