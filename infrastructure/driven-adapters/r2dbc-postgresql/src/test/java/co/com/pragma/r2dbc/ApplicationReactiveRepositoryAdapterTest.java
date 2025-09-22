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
        final UUID id = UUID.randomUUID();
        this.applicationModel = new Application(id, "", BigDecimal.TEN, 12, "test@test.com", 1, 1);

        // El objeto de entidad ahora es un reflejo realista del modelo
        this.applicationEntity = new ApplicationEntity();
        this.applicationEntity.setApplicationId(id);
        this.applicationEntity.setAmount(BigDecimal.TEN);
        this.applicationEntity.setTerm(12);
        this.applicationEntity.setEmail("test@test.com");
        this.applicationEntity.setStatusId(1);
        this.applicationEntity.setLoanTypeId(1);
    }

    @Test
    @DisplayName("Debe guardar una solicitud y retornarla exitosamente")
    void shouldSaveApplicationSuccessfully() {
        // Arrange: Configurar los mocks para el flujo de guardado
        when(this.mapper.map(this.applicationModel, ApplicationEntity.class)).thenReturn(this.applicationEntity);
        when(this.repository.save(this.applicationEntity)).thenReturn(Mono.just(this.applicationEntity));
        when(this.mapper.map(this.applicationEntity, Application.class)).thenReturn(this.applicationModel);

        // Act: Llamar al método que queremos probar
        final Mono<Application> result = this.adapter.save(this.applicationModel);

        // Assert: Verificar que el resultado es el esperado
        StepVerifier.create(result)
                .assertNext(savedApp -> {
                    assertEquals(this.applicationModel.getApplicationId(), savedApp.getApplicationId());
                    assertEquals(this.applicationModel.getEmail(), savedApp.getEmail());
                })
                .verifyComplete();
    }
}
