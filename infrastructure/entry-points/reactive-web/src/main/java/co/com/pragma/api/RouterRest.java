package co.com.pragma.api;

import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RequestPredicates.path;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class RouterRest {
    @Bean
    @RouterOperations({
            @RouterOperation(path = "/api/v1/solicitud", produces = {MediaType.APPLICATION_JSON_VALUE}, method = RequestMethod.POST, beanClass = ApplicationCommandHandler.class, beanMethod = "createLoanApplication"),
            @RouterOperation(path = "/api/v1/solicitud", produces = {MediaType.APPLICATION_JSON_VALUE}, method = RequestMethod.GET, beanClass = ApplicationQueryHandler.class, beanMethod = "getApplicationsForReview")
    })
    public RouterFunction<ServerResponse> routerFunction(ApplicationCommandHandler commandHandler, ApplicationQueryHandler queryHandler) {
        return route().nest(
                path("/api/v1/solicitud"), builder -> builder
                        .route(POST("").and(accept(MediaType.APPLICATION_JSON)), commandHandler::createLoanApplication)
                        .route(GET("").and(accept(MediaType.APPLICATION_JSON)), queryHandler::getApplicationsForReview)
        ).build();
    }
}