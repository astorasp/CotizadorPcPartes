package mx.com.qtx.cotizadorv1ds.config;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import mx.com.qtx.cotizadorv1ds.persistencia.config.JpaConfig;

public class SpringContextProvider {
    
    private static AnnotationConfigApplicationContext context;
    
    /**
     * Configuración para escaneo de componentes
     */
    @Configuration
    @ComponentScan(basePackages = {
        "mx.com.qtx.cotizadorv1ds.servicios"
    })
    static class AppConfig {}
    
    public static void initialize() {
        context = new AnnotationConfigApplicationContext();
        // Registramos la configuración de JPA
        context.register(JpaConfig.class);
        // Registramos la configuración para escaneo de componentes
        context.register(AppConfig.class);
        // Refrescamos el contexto para que se inicialicen los beans
        context.refresh();
    }
    
    public static <T> T getBean(Class<T> beanClass) {
        if (context == null) {
            initialize();
        }
        return context.getBean(beanClass);
    }
    
    public static void close() {
        if (context != null) {
            context.close();
        }
    }
}