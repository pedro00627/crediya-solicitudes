package co.com.pragma.usecase.application;

import co.com.pragma.model.application.Application;
import co.com.pragma.model.application.ApplicationCreationResult;
import reactor.core.publisher.Mono;

public interface CreateLoanApplicationUseCase {
    Mono<ApplicationCreationResult> createLoanApplication(Application application);
}