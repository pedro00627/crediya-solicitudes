package co.com.pragma.api;

import co.com.pragma.api.dto.request.ApplicationRequestRecord;
import co.com.pragma.api.exception.InvalidRequestException;
import co.com.pragma.api.mapper.ApplicationRequestMapper;
import co.com.pragma.api.mapper.IApplicationMapper;
import co.com.pragma.model.application.gateways.CreateLoanApplicationUseCase;
import co.com.pragma.model.log.gateways.LoggerPort;
import co.com.pragma.usecase.application.ApplicationUseCase;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class ApplicationCommandHandler implements IApplicationCommandApi {

    private final ApplicationRequestMapper requestMapper;
    private final IApplicationMapper responseMapper;
    private final CreateLoanApplicationUseCase useCase;
    private final Validator validator;
    private final LoggerPort logger;

    @Override
    public Mono<ServerResponse> createLoanApplication(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(ApplicationRequestRecord.class)
                .flatMap(this::validateRequest)
                .flatMap(requestMapper::toModel)
                .flatMap(useCase::createLoanApplication)
                .doOnSuccess(result -> logger.info("Proceso de creación de solicitud finalizado exitosamente para el ID: {}", result.application().getApplicationId()))
                .map(responseMapper::toResponse)
                .flatMap(response -> ServerResponse
                        .status(HttpStatus.CREATED)
                        .bodyValue(response));
    }

    private Mono<ApplicationRequestRecord> validateRequest(ApplicationRequestRecord request) {
        logger.info("Petición recibida para crear solicitud para el email: {}", logger.maskEmail(request.email()));
        Set<ConstraintViolation<ApplicationRequestRecord>> violations = validator.validate(request);
        if (!violations.isEmpty()) {
            return Mono.error(new InvalidRequestException(violations));
        }
        return Mono.just(request);
    }
}