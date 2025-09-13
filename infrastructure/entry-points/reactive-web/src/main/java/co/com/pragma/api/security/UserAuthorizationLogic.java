package co.com.pragma.api.security;

import co.com.pragma.model.security.RoleConstants;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import reactor.core.publisher.Mono;

import java.util.Set;

/**
 * Contiene la lógica de negocio pura para la autorización de usuarios,
 * desacoplada de las interfaces de Spring Security.
 */
public class UserAuthorizationLogic {

    private static final Set<String> REQUIRED_ROLES = Set.of(RoleConstants.ADMIN, RoleConstants.ADVISOR);

    public Mono<AuthorizationDecision> check(Mono<Authentication> authentication) {
        return authentication
                .filter(Authentication::isAuthenticated)
                .flatMapIterable(Authentication::getAuthorities)
                .map(GrantedAuthority::getAuthority)
                .any(REQUIRED_ROLES::contains)
                .map(AuthorizationDecision::new)
                .defaultIfEmpty(new AuthorizationDecision(false));
    }
}
