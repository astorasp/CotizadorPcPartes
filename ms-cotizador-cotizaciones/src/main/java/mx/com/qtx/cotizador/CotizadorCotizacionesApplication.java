package mx.com.qtx.cotizador;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Clase principal de la aplicación Spring Boot para el microservicio de cotizaciones.
 * <p>
 * Esta clase inicializa y configura el microservicio de cotizaciones del sistema CotizadorPcPartes.
 * El microservicio maneja la creación, gestión y consulta de cotizaciones de componentes de hardware
 * utilizando arquitectura Domain-Driven Design (DDD) con patrones Strategy para diferentes
 * estrategias de cotización e Impuestos con patrón Bridge.
 * </p>
 *
 * <h3>Características principales:</h3>
 * <ul>
 *   <li><strong>Cotización de componentes:</strong> Monitores, Discos Duros, Tarjetas de Video, PCs ensambladas</li>
 *   <li><strong>Múltiples estrategias:</strong> Cotizador A y Cotizador B con diferentes algoritmos</li>
 *   <li><strong>Cálculo de impuestos:</strong> Soporte multi-país (México, USA, Canadá)</li>
 *   <li><strong>Sistema de promociones:</strong> Descuentos planos, por cantidad, ofertas N×M</li>
 *   <li><strong>Arquitectura DDD:</strong> Separación clara de dominio, aplicación e infraestructura</li>
 *   <li><strong>Seguridad JWT:</strong> Integración con microservicio de autenticación</li>
 *   <li><strong>Cache local:</strong> Optimización con Caffeine para componentes y promociones</li>
 * </ul>
 *
 * <h3>Configuraciones habilitadas:</h3>
 * <ul>
 *   <li>{@link EnableScheduling} - Tareas programadas para limpieza de cache y monitoreo</li>
 *   <li>{@link EnableRetry} - Reintentos automáticos para llamadas a servicios externos</li>
 *   <li>{@link SpringBootApplication} - Auto-configuración completa de Spring Boot</li>
 * </ul>
 *
 * @author Subagente3F - [2025-01-17 19:30:00 MST]
 * @version 2.0.0
 * @since 1.0.0
 * @see org.springframework.boot.autoconfigure.SpringBootApplication
 * @see org.springframework.retry.annotation.EnableRetry
 * @see org.springframework.scheduling.annotation.EnableScheduling
 */
@SpringBootApplication
@EnableScheduling
@EnableRetry
public class CotizadorCotizacionesApplication {

    /**
     * Método principal que inicia la aplicación Spring Boot.
     * <p>
     * Este método es el punto de entrada del microservicio. Inicia el contexto de Spring,
     * configura todos los beans, establece las conexiones a la base de datos,
     * inicializa el cache local y pone en marcha los servicios REST.
     * </p>
     *
     * @param args Argumentos de línea de comandos pasados al iniciar la aplicación.
     *             Actualmente no se procesan argumentos específicos.
     */
    public static void main(String[] args) {
        SpringApplication.run(CotizadorCotizacionesApplication.class, args);
    }
}
