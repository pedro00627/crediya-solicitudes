package co.com.pragma.config;

import co.com.pragma.model.config.AppRules;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppRulesConfig {

    @Bean
    public AppRules appRules(final AppRulesProperties properties) {
        return new AppRules(
                properties.clientRoleId(),
                properties.pendingStatusId(),
                properties.terminalStatusIds()
        );
    }
}