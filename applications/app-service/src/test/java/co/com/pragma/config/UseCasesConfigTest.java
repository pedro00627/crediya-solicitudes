package co.com.pragma.config;

import co.com.pragma.model.application.gateways.ApplicationRepository;
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

import static org.junit.jupiter.api.Assertions.assertNotNull;

// Se cargan explícitamente la configuración principal y la de mocks para la prueba.
// Esto nos da control total sobre el contexto de la aplicación para este test.
// Al separar la configuración, ahora solo necesitamos cargar UseCasesConfig.
// MockDependenciesConfig proporcionará todos los beans que UseCasesConfig necesita.
@SpringBootTest(classes = { UseCasesConfig.class, UseCasesConfigTest.MockDependenciesConfig.class })
class UseCasesConfigTest {

    @Autowired
    private ApplicationUseCase applicationUseCase;

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
        public ApplicationRepository applicationRepository() {
            return Mockito.mock(ApplicationRepository.class);
        }

        @Bean
        public LoggerPort loggerPort() {
            return Mockito.mock(LoggerPort.class);
        }

        /**
         * Se proporciona un mock del bean 'AppRules' directamente.
         * Como la configuración real de AppRules no se carga en esta prueba,
         * este mock es la única definición disponible, evitando conflictos.
         */
        @Bean
        @Primary
        public AppRules appRules() {
            return Mockito.mock(AppRules.class);
        }
    }

    @Test
    void applicationUseCaseShouldBeCreated() {
        // La prueba ahora simplemente verifica que el contexto de Spring pudo
        // crear el bean 'applicationUseCase' inyectando todos los mocks.
        assertNotNull(applicationUseCase);
    }
}