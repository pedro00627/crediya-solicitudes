package co.com.pragma.config;

import co.com.pragma.commonutils.LogHelperAdapter;
import co.com.pragma.model.application.gateways.ApplicationGateway;
import co.com.pragma.model.config.AppRules;
import co.com.pragma.model.gateways.NotificationGateway;
import co.com.pragma.model.loantype.gateways.LoanTypeGateway;
import co.com.pragma.model.log.gateways.LoggerPort;
import co.com.pragma.model.status.gateways.StatusGateway;
import co.com.pragma.model.user.gateways.UserGateway;
import co.com.pragma.sqs.sender.config.SQSSenderProperties;
import co.com.pragma.usecase.application.ApplicationUseCase;
import co.com.pragma.usecase.application.CreateLoanApplicationUseCase;
import co.com.pragma.usecase.application.FindApplicationsForReviewUseCase;
import co.com.pragma.usecase.application.FindApplicationsForReviewUseCaseImpl;
import co.com.pragma.usecase.application.UpdateApplicationStatusUseCase;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
@EnableConfigurationProperties(SQSSenderProperties.class)
public class UseCasesConfig {

    @Bean
    public CreateLoanApplicationUseCase applicationUseCase(
            final LoanTypeGateway loanTypeGateway,
            final StatusGateway statusGateway,
            final UserGateway userGateway,
            final ApplicationGateway applicationGateway,
            final LoggerPort logger,
            final AppRules appRules) {
        return new ApplicationUseCase(
                loanTypeGateway,
                statusGateway,
                userGateway,
                applicationGateway,
                logger,
                appRules
        );
    }

    @Bean
    public FindApplicationsForReviewUseCase findApplicationsForReviewUseCase(
            final ApplicationGateway applicationGateway,
            final LoggerPort logger) {
        return new FindApplicationsForReviewUseCaseImpl(applicationGateway, logger);
    }

    @Bean
    public UpdateApplicationStatusUseCase updateApplicationStatusUseCase(
            final ApplicationGateway applicationGateway,
            final LoanTypeGateway loanTypeGateway,
            final StatusGateway statusGateway,
            final UserGateway userGateway,
            final NotificationGateway notificationGateway,
            final LoggerPort logger) {
        return new UpdateApplicationStatusUseCase(
            applicationGateway,
            loanTypeGateway,
            statusGateway,
            userGateway,
            notificationGateway,
            logger
        );
    }


    @Bean
    @Primary // Asegura que este bean sea el preferido si hay múltiples implementaciones de LoggerPort
    public LoggerPort loggerPort() {
        return new LogHelperAdapter();
    }
}