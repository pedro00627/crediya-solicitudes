package co.com.pragma;

import co.com.pragma.config.AppRulesProperties;
import co.com.pragma.r2dbc.config.PostgresqlConnectionProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
@EnableConfigurationProperties({AppRulesProperties.class, PostgresqlConnectionProperties.class})
public class MainApplication {
    public static void main(String[] args) {
        SpringApplication.run(MainApplication.class, args);
    }
}