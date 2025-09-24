package co.com.pragma.config;

import co.com.pragma.model.application.gateways.ApplicationGateway;
import co.com.pragma.model.config.AppRules;
import co.com.pragma.model.loantype.gateways.LoanTypeGateway;
import co.com.pragma.model.log.gateways.LoggerPort;
import co.com.pragma.model.status.gateways.StatusGateway;
import co.com.pragma.model.user.gateways.UserGateway;
import co.com.pragma.usecase.application.ApplicationUseCase;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.junit.jupiter.api.Assertions.assertNotNull;

// Se cargan explícitamente la configuración principal y la de mocks para la prueba.
// Esto nos da control total sobre el contexto de la aplicación para este test.
// Al separar la configuración, ahora solo necesitamos cargar UseCasesConfig.
// MockDependenciesConfig proporcionará todos los beans que UseCasesConfig necesita.
@SpringBootTest(classes = {UseCasesConfig.class, UseCasesConfigTest.MockDependenciesConfig.class})
class UseCasesConfigTest {

    @Autowired
    private ApplicationUseCase applicationUseCase;

    // Usar @MockBean para reemplazar el bean LoggerPort existente con un mock
    @MockitoBean
    private LoggerPort loggerPort; // Spring Boot creará un mock y lo inyectará

    @Test
    void applicationUseCaseShouldBeCreated() {
        // La prueba ahora simplemente verifica que el contexto de Spring pudo
        // crear el bean 'applicationUseCase' inyectando todos los mocks.
        assertNotNull(this.applicationUseCase);
    }

    /**
     * Esta clase de configuración interna proporciona mocks para todas las dependencias
     * externas que necesita UseCasesConfig. Este es el patrón recomendado cuando
     * no se puede o no se desea usar @MockBean.
     */
    @Configuration
    static class MockDependenciesConfig {

        @Bean
        public LoanTypeGateway loanTypeGateway() {
            return Mockito.mock(LoanTypeGateway.class);
        }

        @Bean
        public StatusGateway statusGateway() {
            return Mockito.mock(StatusGateway.class);
        }

        @Bean
        public UserGateway userGateway() {
            return Mockito.mock(UserGateway.class);
        }

        @Bean
        public ApplicationGateway applicationRepository() {
            return Mockito.mock(ApplicationGateway.class);
        }

        @Bean
        @Primary
        public AppRules appRules() {
            return Mockito.mock(AppRules.class);
        }
    }
}
