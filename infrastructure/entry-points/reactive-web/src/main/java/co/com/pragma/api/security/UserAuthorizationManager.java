package co.com.pragma.api.security;

import co.com.pragma.model.security.RoleConstants;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.ReactiveAuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.server.authorization.AuthorizationContext;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Set;

@Component
public class UserAuthorizationManager implements ReactiveAuthorizationManager<AuthorizationContext> {

    // Usamos un Set para una comprobación más limpia y eficiente.
    private static final Set<String> REQUIRED_ROLES = Set.of(RoleConstants.ADMIN, RoleConstants.ADVISOR);

    @Override
    public Mono<AuthorizationDecision> check(Mono<Authentication> authentication, AuthorizationContext object) {
        return authentication
                // 1. Nos aseguramos de que el usuario esté autenticado.
                .filter(Authentication::isAuthenticated)
                // 2. Obtenemos un flujo (Flux) de sus roles/autoridades.
                .flatMapIterable(Authentication::getAuthorities)
                // 3. Convertimos cada autoridad a su representación en String.
                .map(GrantedAuthority::getAuthority)
                // 4. Verificamos si ALGUNO de los roles del usuario está en nuestro Set de roles requeridos.
                .any(REQUIRED_ROLES::contains)
                // 5. Convertimos el resultado booleano (true/false) en una decisión de autorización.
                .map(AuthorizationDecision::new)
                // 6. Si el usuario no estaba autenticado o no tenía roles, se deniega el acceso por defecto.
                .defaultIfEmpty(new AuthorizationDecision(false));
    }
}
