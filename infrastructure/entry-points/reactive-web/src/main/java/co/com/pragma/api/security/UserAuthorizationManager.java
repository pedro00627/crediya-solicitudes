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

import java.util.Collection;

@Component
public class UserAuthorizationManager implements ReactiveAuthorizationManager<AuthorizationContext> {

    @Override
    public Mono<AuthorizationDecision> check(Mono<Authentication> authentication, AuthorizationContext object) {
        return authentication
                .map(auth -> {
                    if (auth instanceof UsernamePasswordAuthenticationToken) {
                        UsernamePasswordAuthenticationToken userAuth = (UsernamePasswordAuthenticationToken) auth;
                        Collection<? extends GrantedAuthority> authorities = userAuth.getAuthorities();

                        // Ejemplo: Permitir acceso si el usuario tiene el rol ADMIN o ADVISOR
                        boolean hasRequiredRole = authorities.stream()
                                .anyMatch(a -> a.getAuthority().equals(RoleConstants.ADMIN) || a.getAuthority().equals(RoleConstants.ADVISOR));

                        return new AuthorizationDecision(hasRequiredRole);
                    }
                    return new AuthorizationDecision(false);
                })
                .defaultIfEmpty(new AuthorizationDecision(false));
    }
}
