package co.com.pragma.model.user.gateways;

import co.com.pragma.model.user.UserRecord;
import reactor.core.publisher.Mono;

public interface UserGateway {
    Mono<UserRecord> findUserByEmail(String email);
}
