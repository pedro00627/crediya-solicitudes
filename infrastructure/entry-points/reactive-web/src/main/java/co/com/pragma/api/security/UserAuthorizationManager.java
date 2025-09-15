package co.com.pragma.api.security;

import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.ReactiveAuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.authorization.AuthorizationContext;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Adaptador que integra la lógica de autorización de negocio con las interfaces de Spring Security.
 * Esta clase es el único punto que depende directamente de ReactiveAuthorizationManager,
 * aislando la lógica de negocio de las particularidades del framework.
 */
@Component
public class UserAuthorizationManager implements ReactiveAuthorizationManager<AuthorizationContext> {

    private final UserAuthorizationLogic authorizationLogic;

    public UserAuthorizationManager() {
        this.authorizationLogic = new UserAuthorizationLogic();
    }

    @Override
    public Mono<AuthorizationDecision> check(Mono<Authentication> authentication, AuthorizationContext object) {
        // Delega la lógica de negocio a la clase pura.
        return this.authorizationLogic.check(authentication);
    }
}
