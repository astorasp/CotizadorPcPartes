package mx.com.qtx.cotizador.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Configuración JPA para el microservicio de cotizaciones.
 * <p>
 * Esta clase configura la capa de persistencia JPA (Java Persistence API) del microservicio.
 * Spring Boot proporciona auto-configuración automática para la mayoría de los componentes JPA,
 * pero esta configuración explícita asegura el control sobre los repositorios y transacciones.
 * </p>
 *
 * <h3>Configuraciones habilitadas:</h3>
 * <ul>
 *   <li>{@link EnableJpaRepositories} - Escanea y registra repositorios JPA en el paquete especificado</li>
 *   <li>{@link EnableTransactionManagement} - Habilita gestión declarativa de transacciones con @Transactional</li>
 * </ul>
 *
 * <h3>Componentes auto-configurados por Spring Boot:</h3>
 * <ul>
 *   <li><strong>DataSource:</strong> Conexión a MySQL configurada en application.yml</li>
 *   <li><strong>EntityManagerFactory:</strong> Fábrica de EntityManagers para operaciones JPA</li>
 *   <li><strong>TransactionManager:</strong> Gestor de transacciones para operaciones de base de datos</li>
 *   <li><strong>Hibernate:</strong> Implementación JPA por defecto con configuración MySQL</li>
 * </ul>
 *
 * @author Subagente3F - [2025-01-17 19:30:00 MST]
 * @version 1.0.0
 * @since 1.0.0
 * @see org.springframework.data.jpa.repository.config.EnableJpaRepositories
 * @see org.springframework.transaction.annotation.EnableTransactionManagement
 */
@Configuration
@EnableJpaRepositories(basePackages = "mx.com.qtx.cotizador.repositorio")
@EnableTransactionManagement
public class JpaConfig {
}
