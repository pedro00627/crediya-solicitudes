package co.com.pragma.config;

import co.com.pragma.model.config.AppRules;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppRulesConfig {

    @Bean
    public AppRules appRules(AppRulesProperties properties) {
        // This bean bridges the gap between the framework-specific properties
        // and the pure domain model object.
        return new AppRules(
                properties.clientRoleId(),
                properties.pendingStatusId(),
                properties.terminalStatusIds()
        );
    }
}