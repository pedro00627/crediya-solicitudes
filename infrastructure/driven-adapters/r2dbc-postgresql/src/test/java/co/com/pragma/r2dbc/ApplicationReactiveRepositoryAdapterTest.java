package co.com.pragma.r2dbc;

import co.com.pragma.model.application.Application;
import co.com.pragma.r2dbc.entity.ApplicationEntity;
import co.com.pragma.r2dbc.interfaces.ApplicationReactiveRepository;
import co.com.pragma.r2dbc.repository.ApplicationReactiveGatewayAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.reactivecommons.utils.ObjectMapper;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApplicationReactiveRepositoryAdapterTest {

    @InjectMocks
    private ApplicationReactiveGatewayAdapter adapter;

    @Mock
    private ApplicationReactiveRepository repository;

    @Mock
    private ObjectMapper mapper;

    private Application applicationModel;
    private ApplicationEntity applicationEntity;

    @BeforeEach
    void setUp() {
        // Arrange: Datos de prueba consistentes
        UUID id = UUID.randomUUID();
        applicationModel = new Application(id, "", BigDecimal.TEN, 12, "test@test.com", 1, 1);

        // El objeto de entidad ahora es un reflejo realista del modelo
        applicationEntity = new ApplicationEntity();
        applicationEntity.setApplicationId(id);
        applicationEntity.setAmount(BigDecimal.TEN);
        applicationEntity.setTerm(12);
        applicationEntity.setEmail("test@test.com");
        applicationEntity.setStatusId(1);
        applicationEntity.setLoanTypeId(1);
    }

    @Test
    @DisplayName("Debe guardar una solicitud y retornarla exitosamente")
    void shouldSaveApplicationSuccessfully() {
        // Arrange: Configurar los mocks para el flujo de guardado
        when(mapper.map(applicationModel, ApplicationEntity.class)).thenReturn(applicationEntity);
        when(repository.save(applicationEntity)).thenReturn(Mono.just(applicationEntity));
        when(mapper.map(applicationEntity, Application.class)).thenReturn(applicationModel);

        // Act: Llamar al método que queremos probar
        Mono<Application> result = adapter.save(applicationModel);

        // Assert: Verificar que el resultado es el esperado
        StepVerifier.create(result)
                .assertNext(savedApp -> {
                    assertEquals(applicationModel.getApplicationId(), savedApp.getApplicationId());
                    assertEquals(applicationModel.getEmail(), savedApp.getEmail());
                })
                .verifyComplete();
    }
}
