package mx.com.qtx.seguridad.scheduler;

import mx.com.qtx.seguridad.repository.AccesoRepository;
import mx.com.qtx.seguridad.entity.Acceso;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Job de Quartz para limpiar sesiones expiradas
 * Busca registros activos donde la diferencia entre fecha actual y fecha de inicio
 * sea mayor al tiempo de vida configurado del access token
 */
@Component
public class SessionCleanupJob implements Job {

    private static final Logger logger = LoggerFactory.getLogger(SessionCleanupJob.class);

    @Autowired
    private AccesoRepository accesoRepository;

    @Value("${jwt.access-token.expiration:300000}")
    private long accessTokenDurationMs;

    @Override
    @Transactional
    public void execute(JobExecutionContext context) throws JobExecutionException {
        logger.debug("🧹 Iniciando limpieza de sesiones expiradas - Duración token: {}ms ({}s)", 
            accessTokenDurationMs, accessTokenDurationMs / 1000);
        
        try {
            // Calcular fecha de expiración basada en duración del access token
            long accessTokenDurationSeconds = accessTokenDurationMs / 1000;
            LocalDateTime expirationTime = LocalDateTime.now().minusSeconds(accessTokenDurationSeconds);
            
            logger.debug("⏰ Buscando sesiones iniciadas antes de: {}", expirationTime);
            
            // Buscar sesiones activas que iniciaron antes del tiempo de expiración
            List<Acceso> sesionesExpiradas = accesoRepository.findByActivoTrueAndFechaInicioBefore(expirationTime);

            logger.debug("🔍 Encontradas {} sesiones potencialmente expiradas", sesionesExpiradas.size());
            
            if (sesionesExpiradas.isEmpty()) {
                logger.debug("✅ No se encontraron sesiones expiradas para limpiar");
                return;
            }
            
            // Marcar sesiones como inactivas
            int sesionesLimpiadas = 0;
            LocalDateTime ahora = LocalDateTime.now();
            
            for (Acceso sesion : sesionesExpiradas) {
                // Verificar que realmente está expirada
                long tiempoTranscurridoMs = java.time.Duration.between(sesion.getFechaInicio(), ahora).toMillis();
                
                if (tiempoTranscurridoMs > accessTokenDurationMs) {
                    sesion.setActivo(false);
                    sesion.setFechaFin(ahora);
                    sesionesLimpiadas++;
                    
                    logger.debug("Sesión {} marcada como inactiva - Usuario: {}, Iniciada: {}, Tiempo transcurrido: {}ms", 
                        sesion.getIdSesion(), 
                        sesion.getUsuarioId(), 
                        sesion.getFechaInicio(),
                        tiempoTranscurridoMs);
                }
            }
            
            // Guardar cambios
            if (sesionesLimpiadas > 0) {
                accesoRepository.saveAll(sesionesExpiradas);
                logger.info("🎯 Limpieza de sesiones completada - {} sesiones marcadas como inactivas", sesionesLimpiadas);
            } else {
                logger.info("⚠️ No se encontraron sesiones que necesiten limpieza después de la verificación detallada");
            }
            
        } catch (Exception e) {
            logger.error("❌ Error durante la limpieza de sesiones expiradas: {}", e.getMessage(), e);
            throw new JobExecutionException("Error en limpieza de sesiones", e);
        }
    }
}