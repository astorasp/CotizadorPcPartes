package mx.com.qtx.cotizador.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Configuración JPA para el microservicio de gestión de pedidos.
 * <p>
 * Esta clase configura la capa de persistencia JPA (Java Persistence API) del microservicio
 * de pedidos. Spring Boot proporciona auto-configuración automática para la mayoría de los
 * componentes JPA, pero esta configuración explícita asegura el control sobre los repositorios
 * y transacciones específicas del dominio de pedidos.
 * </p>
 *
 * <h3>Entidades Gestionadas:</h3>
 * <ul>
 *   <li>{@link mx.com.qtx.cotizador.entidad.Pedido} - Pedidos generados desde cotizaciones</li>
 *   <li>{@link mx.com.qtx.cotizador.entidad.DetallePedido} - Detalles de componentes en pedidos</li>
 *   <li>{@link mx.com.qtx.cotizador.entidad.Proveedor} - Proveedores asociados a pedidos</li>
 *   <li>{@link mx.com.qtx.cotizador.entidad.Componente} - Referencias a componentes (sincronizadas)</li>
 *   <li>{@link mx.com.qtx.cotizador.entidad.Cotizacion} - Referencias a cotizaciones (sincronizadas)</li>
 *   <li>{@link mx.com.qtx.cotizador.entidad.PcParte} - Relaciones PC-Componente</li>
 * </ul>
 *
 * <h3>Configuraciones habilitadas:</h3>
 * <ul>
 *   <li>{@link EnableJpaRepositories} - Escanea y registra repositorios JPA en el paquete especificado</li>
 *   <li>{@link EnableTransactionManagement} - Habilita gestión declarativa de transacciones</li>
 * </ul>
 *
 * <h3>Estrategia de Persistencia:</h3>
 * <p>
 * El microservicio utiliza una estrategia híbrida de persistencia:
 * </p>
 * <ul>
 *   <li><strong>Datos propios:</strong> Pedidos, detalles de pedido y proveedores se persisten localmente</li>
 *   <li><strong>Datos referenciados:</strong> Componentes y cotizaciones se sincronizan vía eventos Kafka</li>
 *   <li><strong>Transaccionalidad:</strong> Operaciones críticas como generación de pedidos requieren transacciones ACID</li>
 * </ul>
 */
@Configuration
@EnableJpaRepositories(basePackages = "mx.com.qtx.cotizador.repositorio")
@EnableTransactionManagement
public class JpaConfig {
}
