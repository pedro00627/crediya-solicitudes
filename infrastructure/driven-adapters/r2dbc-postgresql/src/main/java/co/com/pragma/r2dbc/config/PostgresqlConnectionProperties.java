package co.com.pragma.r2dbc.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "adapters.r2dbc")
public record PostgresqlConnectionProperties(
        String host,
        int port,
        String database,
        String schema,
        String username,
        String password) {
}
