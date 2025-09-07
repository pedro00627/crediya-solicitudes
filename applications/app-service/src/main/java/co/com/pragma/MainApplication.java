package co.com.pragma;

import co.com.pragma.config.AppRulesProperties;
import co.com.pragma.r2dbc.config.PostgresqlConnectionProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;

// Se especifica el paquete base para el escaneo de componentes.
// Esto asegura que Spring descubra los beans en todas las capas, incluyendo la capa API.
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