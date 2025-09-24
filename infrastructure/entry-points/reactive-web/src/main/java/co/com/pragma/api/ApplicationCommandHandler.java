package co.com.pragma.api;

import co.com.pragma.api.dto.request.ApplicationRequestRecord;
import co.com.pragma.api.exception.InvalidRequestException;
import co.com.pragma.api.mapper.IApplicationRequestMapper;
import co.com.pragma.api.mapper.IApplicationResponseHandler;
import co.com.pragma.model.log.gateways.LoggerPort;
import co.com.pragma.security.api.JWTAuthenticationFilter;
import co.com.pragma.usecase.application.CreateLoanApplicationUseCase;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

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
    public Mono<ServerResponse> createLoanApplication(final ServerRequest serverRequest) {
        return serverRequest.bodyToMono(ApplicationRequestRecord.class)
                .zipWith(serverRequest.principal())
                .flatMap(tuple -> this.validateTokenEmail(tuple.getT1(), tuple.getT2()))
                .flatMap(this::validateRequest)
                .flatMap(this.requestMapper::toModel)
                .flatMap(this.useCase::createLoanApplication) // 1. Se llama al caso de uso
                .switchIfEmpty(Mono.defer(() -> { // 2. Se maneja el caso en que el Mono del caso de uso esté vacío
                    final String errorMessage = "El caso de uso no produjo ningún resultado (Mono vacío).";
                    this.logger.error(errorMessage, new IllegalStateException(errorMessage));
                    return Mono.error(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno al procesar la solicitud."));
                }))
                .doOnNext(result -> // 3. Efecto secundario (log) solo si hay un resultado exitoso
                        this.logger.info("Proceso de creación de solicitud finalizado exitosamente para el ID: {}", result.application().getApplicationId())
                ).contextWrite(context -> {
                    final var authHeaders = serverRequest.headers().header(JWTAuthenticationFilter.AUTH_TOKEN_KEY);
                    if (!authHeaders.isEmpty()) {
                        return context.put(JWTAuthenticationFilter.AUTH_TOKEN_KEY, authHeaders.getFirst());
                    }
                    this.logger.warn("No JWT authentication header found in request");
                    return context;
                })

                .flatMap(this.responseHandler::buildCreationResponse);
    }

    private Mono<ApplicationRequestRecord> validateTokenEmail(final ApplicationRequestRecord request, final Principal principal) {
        final String tokenEmail = principal.getName();
        if (!tokenEmail.equals(request.email())) {
            this.logger.warn("El email del token ({}) no coincide con el email de la solicitud ({})", this.logger.maskEmail(tokenEmail), this.logger.maskEmail(request.email()));
            return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN, "El correo electrónico de la solicitud no coincide con el del token."));
        }
        return Mono.just(request);
    }

    private Mono<ApplicationRequestRecord> validateRequest(final ApplicationRequestRecord request) {
        this.logger.info("Validando petición para crear solicitud para el email: {}", this.logger.maskEmail(request.email()));
        final Set<ConstraintViolation<ApplicationRequestRecord>> violations = this.validator.validate(request);
        if (violations.isEmpty()) {
            return Mono.just(request);
        }
        final String errors = violations.stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .collect(Collectors.joining(", "));
        this.logger.warn("La solicitud para {} contiene datos inválidos. Violaciones: {}", this.logger.maskEmail(request.email()), errors);
        return Mono.error(new InvalidRequestException(violations));
    }
}
