package co.com.pragma.r2dbc.interfaces;

import co.com.pragma.r2dbc.entity.ApplicationEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface ApplicationReactiveRepository extends ReactiveCrudRepository<ApplicationEntity, String>, ReactiveQueryByExampleExecutor<ApplicationEntity> {

    // Se define la consulta de forma declarativa. Spring Data R2DBC se encargará
    // de la implementación y el manejo de parámetros.
    @Query("SELECT s.* FROM solicitudes.solicitud s WHERE s.documento_identidad = :documentId AND s.id_estado NOT IN (:statusIds)")
    Flux<ApplicationEntity> findOpenApplicationsByDocumentId(String documentId, List<Integer> statusIds);

    @Query("SELECT s.* FROM solicitudes.solicitud s " +
           "INNER JOIN solicitudes.estados es ON s.id_estado = es.id_estado " +
           "WHERE es.nombre IN (:statuses)")
    Flux<ApplicationEntity> findByStatusIn(List<String> statuses, Pageable pageable);

    @Query("SELECT COUNT(s.id_solicitud) FROM solicitudes.solicitud s " +
           "INNER JOIN solicitudes.estados es ON s.id_estado = es.id_estado " +
           "WHERE es.nombre IN (:statuses)")
    Mono<Long> countByStatusIn(List<String> statuses);
}