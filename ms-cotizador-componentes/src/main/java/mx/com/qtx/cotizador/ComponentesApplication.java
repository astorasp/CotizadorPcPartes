package mx.com.qtx.cotizador;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Clase principal de la aplicación Spring Boot para el microservicio de componentes.
 * Esta aplicación proporciona servicios REST para la gestión de componentes de PC,
 * incluyendo cotizaciones y promociones.
 *
 * La aplicación está configurada con las siguientes características:
 * - Programación de tareas (@EnableScheduling)
 * - Reintentos automáticos (@EnableRetry)
 * - Procesamiento asíncrono (@EnableAsync)
 *
 * @author [Nombre del autor]
 * @version 1.0
 */
@SpringBootApplication
@EnableScheduling
@EnableRetry
@EnableAsync
public class ComponentesApplication {

	/**
	 * Método principal que inicia la aplicación Spring Boot.
	 * Crea y ejecuta el contexto de la aplicación con la configuración por defecto.
	 *
	 * @param args Argumentos de línea de comandos pasados a la aplicación
	 */
    public static void main(String[] args) {
        SpringApplication.run(ComponentesApplication.class, args);
    }
}
