package co.com.pragma.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "app.rules")
public record AppRules(
        Integer clientRoleId,
        Integer pendingStatusId,
        List<Integer> terminalStatusIds
) {}