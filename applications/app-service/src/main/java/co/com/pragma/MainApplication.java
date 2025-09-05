package co.com.pragma;

import co.com.pragma.config.AppRulesProperties;
import co.com.pragma.r2dbc.config.PostgresqlConnectionProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableCaching
// Se centraliza la habilitación de todas las propiedades de configuración aquí.
// Esto asegura que estén disponibles en el contexto de la aplicación.
@EnableConfigurationProperties({AppRulesProperties.class, PostgresqlConnectionProperties.class})
public class MainApplication {
    public static void main(String[] args) {
        SpringApplication.run(MainApplication.class, args);
    }
}