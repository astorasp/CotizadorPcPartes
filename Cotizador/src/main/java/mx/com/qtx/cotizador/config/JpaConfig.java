package mx.com.qtx.cotizador.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Configuración JPA simplificada para Spring Boot.
 * <p>
 * Spring Boot auto-configura DataSource, EntityManagerFactory y TransactionManager
 * basándose en application.yml. Solo especificamos el paquete de repositorios.
 * </p>
 */
@Configuration
@EnableJpaRepositories(basePackages = "mx.com.qtx.cotizador.repositorio")
@EnableTransactionManagement
public class JpaConfig {
}
