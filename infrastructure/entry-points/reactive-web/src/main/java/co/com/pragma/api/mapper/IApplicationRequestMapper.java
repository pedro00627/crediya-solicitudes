package co.com.pragma.api.mapper;

import co.com.pragma.api.dto.request.ApplicationRequestRecord;
import co.com.pragma.model.application.Application;
import reactor.core.publisher.Mono;

/**
 * Interfaz para el mapeador de solicitudes de creación de aplicaciones.
 * Abstrae la conversión de un DTO de solicitud a un modelo de dominio.
 */
public interface IApplicationRequestMapper {
    Mono<Application> toModel(ApplicationRequestRecord requestRecord);
}
