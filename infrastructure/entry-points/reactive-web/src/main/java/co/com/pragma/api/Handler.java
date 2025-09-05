package co.com.pragma.api;

import co.com.pragma.api.dto.request.ApplicationRequestRecord;
import co.com.pragma.api.exception.InvalidRequestException;
import co.com.pragma.api.mapper.IApplicationMapper;
import co.com.pragma.commonutils.LogHelper;
import co.com.pragma.usecase.application.ApplicationUseCase;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class Handler {
    private final IApplicationMapper mapper;
    private final ApplicationUseCase useCase;
    private final Validator validator;

    public Mono<ServerResponse> createLoanApplication(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(ApplicationRequestRecord.class)
                .doOnNext(this::validateRequest)
                .map(mapper::toModel)
                .flatMap(useCase::createLoanApplication)
                .doOnSuccess(result -> log.info("Proceso de creación de solicitud finalizado exitosamente para el ID: {}",
                        result.application().getApplicationId()))
                .map(mapper::toResponse)
                .flatMap(response -> ServerResponse.status(HttpStatus.CREATED).bodyValue(response));
    }

    private void validateRequest(ApplicationRequestRecord request) {
        log.info("Petición recibida para crear solicitud para el email: {}", LogHelper.maskEmail(request.email()));
        Set<ConstraintViolation<ApplicationRequestRecord>> violations = validator.validate(request);
        if (!violations.isEmpty()) {
            throw new InvalidRequestException(violations);
        }
    }
}