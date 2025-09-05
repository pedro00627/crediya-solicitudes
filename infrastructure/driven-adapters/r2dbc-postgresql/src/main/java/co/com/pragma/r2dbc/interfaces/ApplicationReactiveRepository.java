package co.com.pragma.r2dbc.interfaces;

import co.com.pragma.r2dbc.entity.ApplicationEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

import java.util.List;

public interface ApplicationReactiveRepository extends ReactiveCrudRepository<ApplicationEntity, String>, ReactiveQueryByExampleExecutor<ApplicationEntity> {

    // Se define la consulta de forma declarativa. Spring Data R2DBC se encargará
    // de la implementación y el manejo de parámetros.
    @Query("SELECT * FROM solicitudes.solicitud WHERE documento_identidad = :documentId AND id_estado NOT IN (:statusIds)")
    Flux<ApplicationEntity> findOpenApplicationsByDocumentId(String documentId, List<Integer> statusIds);
}