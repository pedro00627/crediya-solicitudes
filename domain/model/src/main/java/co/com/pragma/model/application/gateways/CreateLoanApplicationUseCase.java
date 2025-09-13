package co.com.pragma.model.application.gateways;

import co.com.pragma.model.application.Application;
import co.com.pragma.model.application.ApplicationCreationResult;
import reactor.core.publisher.Mono;

public interface CreateLoanApplicationUseCase {
    Mono<ApplicationCreationResult> createLoanApplication(Application application);
}