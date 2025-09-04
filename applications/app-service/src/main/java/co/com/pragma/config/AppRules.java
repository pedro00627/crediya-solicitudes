package co.com.pragma.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.rules")
@Getter
@Setter
public class AppRules {
    private Integer clientRoleId;
    private Integer pendingStatusId;
}