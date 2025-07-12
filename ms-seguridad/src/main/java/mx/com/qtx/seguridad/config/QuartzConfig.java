package mx.com.qtx.seguridad.config;

import mx.com.qtx.seguridad.scheduler.SessionCleanupJob;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import jakarta.annotation.PostConstruct;

/**
 * Configuración de Quartz para programar jobs
 */
@Configuration
public class QuartzConfig {

    private static final Logger logger = LoggerFactory.getLogger(QuartzConfig.class);

    @Value("${session.cleanup.interval.seconds:15}")
    private int cleanupIntervalSeconds;

    @Value("${session.cleanup.enabled:true}")
    private boolean cleanupEnabled;

    @PostConstruct
    public void init() {
        logger.debug("🚀 QuartzConfig inicializándose - Cleanup habilitado: {}, Intervalo: {}s", 
                   cleanupEnabled, cleanupIntervalSeconds);
        logger.debug("🔧 DEBUG: Variables cargadas - cleanupEnabled={}, cleanupIntervalSeconds={}", 
                    cleanupEnabled, cleanupIntervalSeconds);
    }

    /**
     * Define el JobDetail para el job de limpieza de sesiones
     */
    @Bean
    public JobDetail sessionCleanupJobDetail() {
        logger.debug("📋 Configurando JobDetail para limpieza de sesiones - Habilitado: {}", cleanupEnabled);
        
        return JobBuilder.newJob(SessionCleanupJob.class)
                .withIdentity("sessionCleanupJob", "sessionManagement")
                .withDescription("Job para limpiar sesiones expiradas")
                .storeDurably()
                .build();
    }

    /**
     * Define el Trigger para el job de limpieza de sesiones
     * Se ejecuta cada X segundos según la configuración
     */
    @Bean
    public Trigger sessionCleanupTrigger(JobDetail sessionCleanupJobDetail) {
        if (!cleanupEnabled) {
            logger.warn("⚠️ Job de limpieza de sesiones DESHABILITADO por configuración");
            // Si está deshabilitado, crear un trigger que nunca se ejecute
            return TriggerBuilder.newTrigger()
                    .forJob(sessionCleanupJobDetail)
                    .withIdentity("sessionCleanupTrigger", "sessionManagement")
                    .withDescription("Trigger deshabilitado para limpieza de sesiones")
                    .startAt(DateBuilder.futureDate(1, DateBuilder.IntervalUnit.YEAR))
                    .build();
        }

        logger.info("⏰ Configurando Trigger para limpieza de sesiones - Intervalo: {} segundos", cleanupIntervalSeconds);

        return TriggerBuilder.newTrigger()
                .forJob(sessionCleanupJobDetail)
                .withIdentity("sessionCleanupTrigger", "sessionManagement")
                .withDescription("Trigger para limpieza de sesiones cada " + cleanupIntervalSeconds + " segundos")
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                        .withIntervalInSeconds(cleanupIntervalSeconds)
                        .repeatForever())
                .startNow()
                .build();
    }
}