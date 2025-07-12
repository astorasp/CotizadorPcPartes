package mx.com.qtx.seguridad.controller;

import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Controlador para monitoreo del sistema
 */
@RestController
@RequestMapping("/monitoring")
public class MonitoringController {

    private static final Logger logger = LoggerFactory.getLogger(MonitoringController.class);

    @Autowired
    private Scheduler scheduler;

    /**
     * Endpoint para verificar el estado del job de limpieza de sesiones
     */
    @GetMapping("/session-cleanup-job")
    public ResponseEntity<Map<String, Object>> getSessionCleanupJobStatus() {
        Map<String, Object> status = new HashMap<>();
        
        try {
            JobKey jobKey = new JobKey("sessionCleanupJob", "sessionManagement");
            TriggerKey triggerKey = new TriggerKey("sessionCleanupTrigger", "sessionManagement");
            
            // Verificar si el job existe
            JobDetail jobDetail = scheduler.getJobDetail(jobKey);
            Trigger trigger = scheduler.getTrigger(triggerKey);
            
            if (jobDetail == null) {
                status.put("status", "NOT_CONFIGURED");
                status.put("message", "Job no está configurado");
                return ResponseEntity.ok(status);
            }
            
            // Estado del scheduler
            status.put("schedulerRunning", scheduler.isStarted() && !scheduler.isShutdown());
            status.put("schedulerName", scheduler.getSchedulerName());
            
            // Información del job
            status.put("jobExists", true);
            status.put("jobKey", jobKey.toString());
            status.put("jobDescription", jobDetail.getDescription());
            status.put("jobClass", jobDetail.getJobClass().getSimpleName());
            
            // Información del trigger
            if (trigger != null) {
                status.put("triggerExists", true);
                status.put("triggerKey", triggerKey.toString());
                status.put("triggerDescription", trigger.getDescription());
                status.put("triggerState", scheduler.getTriggerState(triggerKey).toString());
                
                // Fechas importantes
                Date nextFireTime = trigger.getNextFireTime();
                Date previousFireTime = trigger.getPreviousFireTime();
                
                if (nextFireTime != null) {
                    LocalDateTime nextFire = nextFireTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
                    status.put("nextExecution", nextFire.toString());
                }
                
                if (previousFireTime != null) {
                    LocalDateTime previousFire = previousFireTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
                    status.put("lastExecution", previousFire.toString());
                }
                
                // Información específica del SimpleScheduleBuilder
                if (trigger instanceof SimpleTrigger) {
                    SimpleTrigger simpleTrigger = (SimpleTrigger) trigger;
                    status.put("repeatInterval", simpleTrigger.getRepeatInterval() + "ms");
                    status.put("repeatCount", simpleTrigger.getRepeatCount());
                    status.put("timesTriggered", simpleTrigger.getTimesTriggered());
                }
            } else {
                status.put("triggerExists", false);
            }
            
            status.put("status", "ACTIVE");
            status.put("timestamp", LocalDateTime.now().toString());
            
            logger.info("🔍 Estado del job consultado: {}", status);
            
        } catch (SchedulerException e) {
            logger.error("❌ Error al consultar estado del job: {}", e.getMessage(), e);
            status.put("status", "ERROR");
            status.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(status);
        }
        
        return ResponseEntity.ok(status);
    }

    /**
     * Endpoint para ejecutar manualmente el job de limpieza
     */
    @GetMapping("/session-cleanup-job/trigger")
    public ResponseEntity<Map<String, Object>> triggerSessionCleanupJob() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            JobKey jobKey = new JobKey("sessionCleanupJob", "sessionManagement");
            
            // Verificar si el job existe
            if (!scheduler.checkExists(jobKey)) {
                result.put("success", false);
                result.put("message", "Job no está configurado");
                return ResponseEntity.badRequest().body(result);
            }
            
            // Ejecutar el job manualmente
            scheduler.triggerJob(jobKey);
            
            result.put("success", true);
            result.put("message", "Job de limpieza de sesiones ejecutado manualmente");
            result.put("timestamp", LocalDateTime.now().toString());
            
            logger.info("🚀 Job de limpieza de sesiones ejecutado manualmente");
            
        } catch (SchedulerException e) {
            logger.error("❌ Error al ejecutar job manualmente: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        }
        
        return ResponseEntity.ok(result);
    }
}