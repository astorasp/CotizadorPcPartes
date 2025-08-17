package mx.com.qtx.seguridad;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SeguridadApplication {

    public static void main(String[] args) {
        SpringApplication.run(SeguridadApplication.class, args);
    }
}