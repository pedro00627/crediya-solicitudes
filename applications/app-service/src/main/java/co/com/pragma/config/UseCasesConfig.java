package co.com.pragma.config;

import co.com.pragma.model.application.gateways.ApplicationRepository;
import co.com.pragma.model.loantype.gateways.LoanTypeGateway;
import co.com.pragma.model.status.gateways.StatusGateway;
import co.com.pragma.model.user.gateways.UserGateway;
import co.com.pragma.usecase.application.ApplicationUseCase;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

@Configuration
@ComponentScan(basePackages = "co.com.pragma.usecase",
        includeFilters = {
                @ComponentScan.Filter(type = FilterType.REGEX, pattern = "^.+UseCase$")
        },
        useDefaultFilters = false)
public class UseCasesConfig {

        @Bean
        public ApplicationUseCase applicationUseCase(
                @Qualifier("loanTypeReactiveGatewayAdapter") LoanTypeGateway loanTypeGateway,
                @Qualifier("statusReactiveGatewayAdapter") StatusGateway statusGateway,
                UserGateway userGateway,
                ApplicationRepository applicationRepository) {
                return new ApplicationUseCase(
                        loanTypeGateway,
                        statusGateway,
                        userGateway,
                        applicationRepository
                );
        }
}