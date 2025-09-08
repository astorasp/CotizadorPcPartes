package mx.com.qtx.cotizador;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Clase principal del microservicio CotizadorPcPartes - Gestión de Pedidos.
 * <p>
 * Este microservicio es responsable de la gestión completa del ciclo de vida de pedidos
 * dentro del sistema CotizadorPcPartes. Maneja la creación, consulta y gestión de pedidos
 * basados en cotizaciones existentes, además de la administración de proveedores.
 * </p>
 *
 * <h3>Casos de Uso Principales:</h3>
 * <ul>
 *   <li><strong>CU 5.2:</strong> Generar pedido desde cotización</li>
 *   <li><strong>CU 5.3:</strong> Consultar pedidos</li>
 *   <li><strong>CU 4.1:</strong> Agregar proveedor</li>
 *   <li><strong>CU 4.2:</strong> Modificar proveedor</li>
 *   <li><strong>CU 4.3:</strong> Consultar proveedores</li>
 *   <li><strong>CU 4.4:</strong> Eliminar proveedor</li>
 * </ul>
 *
 * <h3>Características Técnicas:</h3>
 * <ul>
 *   <li>{@link EnableScheduling} - Habilitado para tareas programadas</li>
 *   <li>{@link EnableRetry} - Soporte para reintentos automáticos</li>
 *   <li>{@link SpringBootApplication} - Auto-configuración completa de Spring Boot</li>
 * </ul>
 *
 * <h3>Dependencias Externas:</h3>
 * <ul>
 *   <li><strong>ms-cotizador-cotizaciones:</strong> Para obtener información de cotizaciones</li>
 *   <li><strong>ms-cotizador-componentes:</strong> Para datos de componentes</li>
 *   <li><strong>ms-seguridad:</strong> Para autenticación y autorización JWT</li>
 * </ul>
 *
 * <h3>Arquitectura de Seguridad:</h3>
 * <p>
 * Implementa control de acceso basado en roles con los siguientes niveles:
 * </p>
 * <ul>
 *   <li><strong>ADMIN:</strong> Acceso completo a todas las operaciones</li>
 *   <li><strong>GERENTE:</strong> Gestión comercial y aprobación</li>
 *   <li><strong>VENDEDOR:</strong> Generación y consulta de pedidos</li>
 *   <li><strong>INVENTARIO:</strong> Gestión de inventario y proveedores</li>
 *   <li><strong>CONSULTOR:</strong> Solo lectura para análisis</li>
 * </ul>
 *
 * @author Sistema CotizadorPcPartes
 * @version 1.0
 * @since 2024
 */
@SpringBootApplication
@EnableScheduling
@EnableRetry
public class PedidosApplication {

    /**
     * Método principal que inicia la aplicación Spring Boot.
     * <p>
     * Este método delega la inicialización completa de la aplicación a Spring Boot,
     * incluyendo la configuración automática de componentes, beans, seguridad,
     * persistencia y comunicación con otros microservicios.
     * </p>
     *
     * @param args Argumentos de línea de comandos pasados al iniciar la aplicación
     */
    public static void main(String[] args) {
        SpringApplication.run(PedidosApplication.class, args);
    }
}
