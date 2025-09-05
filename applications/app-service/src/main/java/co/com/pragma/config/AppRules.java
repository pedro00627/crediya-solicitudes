package co.com.pragma.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.rules")
public record AppRules(
        Integer clientRoleId,
        Integer pendingStatusId
) {
}