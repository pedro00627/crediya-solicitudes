package co.com.pragma.config;

import co.com.pragma.model.application.gateways.ApplicationGateway;
import co.com.pragma.model.config.AppRules;
import co.com.pragma.model.loantype.gateways.LoanTypeGateway;
import co.com.pragma.model.log.gateways.LoggerPort;
import co.com.pragma.model.status.gateways.StatusGateway;
import co.com.pragma.model.user.gateways.UserGateway;
import co.com.pragma.usecase.application.ApplicationUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCasesConfig {

    @Bean
    public ApplicationUseCase applicationUseCase(
            LoanTypeGateway loanTypeGateway,
            StatusGateway statusGateway,
            UserGateway userGateway,
            ApplicationGateway applicationGateway,
            LoggerPort logger,
            AppRules appRules) {
        return new ApplicationUseCase(
                loanTypeGateway,
                statusGateway,
                userGateway,
                applicationGateway,
                logger,
                appRules
        );
    }
}