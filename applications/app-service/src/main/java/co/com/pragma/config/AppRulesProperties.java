package co.com.pragma.config;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
@ConfigurationProperties(prefix = "app.rules")
public record AppRulesProperties(
        @NotNull Integer clientRoleId,
        @NotNull Integer pendingStatusId,
        @NotNull List<Integer> terminalStatusIds
) {}