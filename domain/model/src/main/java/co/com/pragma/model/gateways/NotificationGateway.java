package co.com.pragma.model.gateways;

import co.com.pragma.model.events.ApplicationStatusEvent;
import reactor.core.publisher.Mono;

/**
 * Gateway para envío de notificaciones de cambio de estado
 * Implementado por el driven-adapter de SQS
 */
public interface NotificationGateway {

    /**
     * Envía un evento de cambio de estado a la cola de notificaciones
     * @param event Evento con los datos del cambio de estado
     * @return Mono con el ID del mensaje enviado
     */
    Mono<String> sendApplicationStatusChange(ApplicationStatusEvent event);
}