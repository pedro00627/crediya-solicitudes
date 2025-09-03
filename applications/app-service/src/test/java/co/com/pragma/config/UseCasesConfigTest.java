package co.com.pragma.config;

import co.com.pragma.model.application.gateways.ApplicationRepository;
import co.com.pragma.model.loantype.gateways.LoanTypeGateway;
import co.com.pragma.model.status.gateways.StatusGateway;
import co.com.pragma.model.user.gateways.UserGateway;
import co.com.pragma.usecase.application.ApplicationUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {UseCasesConfig.class, UseCasesConfigTest.MockDependenciesConfig.class})
class UseCasesConfigTest {

    @TestConfiguration
    static class MockDependenciesConfig {
        @Bean("loanTypeReactiveGatewayAdapter")
        public LoanTypeGateway loanTypeGateway() {
            return Mockito.mock(LoanTypeGateway.class);
        }

        @Bean("statusReactiveGatewayAdapter")
        public StatusGateway statusGateway() {
            return Mockito.mock(StatusGateway.class);
        }

        @Bean
        public UserGateway userGateway() {
            return Mockito.mock(UserGateway.class);
        }

        @Bean
        public ApplicationRepository applicationRepository() {
            return Mockito.mock(ApplicationRepository.class);
        }
    }

    @Autowired
    private ApplicationContext context;

    @Test
    void applicationUseCaseShouldBeCreated() {
        // Act: Intentamos obtener el bean del ApplicationUseCase del contexto.
        ApplicationUseCase useCase = context.getBean(ApplicationUseCase.class);

        // Assert: Si la línea anterior no lanzó una excepción, el bean se creó exitosamente.
        assertNotNull(useCase, "El bean de ApplicationUseCase no debería ser nulo.");
    }
}