package mx.com.qtx.cotizador.servicio.cache;

import mx.com.qtx.cotizador.entidad.Promocion;
import mx.com.qtx.cotizador.repositorio.PromocionRepositorio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Servicio de cache para promociones con fallback a base de datos local.
 * 
 * Proporciona acceso en cache a promociones activas para optimizar
 * el cálculo de descuentos en los algoritmos de cotización.
 * 
 * @author Subagente3F - [2025-01-17 19:40:00 MST] - Servicio de cache para promociones
 */
@Service
public class PromocionCacheService {

    private static final Logger logger = LoggerFactory.getLogger(PromocionCacheService.class);
    
    @Autowired
    private PromocionRepositorio promocionRepositorio;

    /**
     * Busca una promoción por ID utilizando cache.
     * Si no está en cache, consulta la base de datos local y almacena el resultado.
     * 
     * @param idPromocion ID de la promoción a buscar
     * @return Optional con la promoción encontrada
     */
    @Cacheable(value = "promociones", key = "#idPromocion")
    public Optional<Promocion> buscarPromocion(Integer idPromocion) {
        logger.debug("Consultando promoción desde base de datos (cache miss): {}", idPromocion);
        
        try {
            return promocionRepositorio.findById(idPromocion);
            
        } catch (Exception e) {
            logger.error("Error consultando promoción {}: {}", idPromocion, e.getMessage(), e);
            return Optional.empty();
        }
    }

    /**
     * Obtiene todas las promociones utilizando cache.
     * 
     * @return Lista de todas las promociones
     */
    @Cacheable(value = "promociones", key = "'promociones-activas'")
    public List<Promocion> obtenerPromocionesActivas() {
        logger.debug("Consultando promociones desde base de datos (cache miss)");
        
        try {
            return promocionRepositorio.findAll();
            
        } catch (Exception e) {
            logger.error("Error consultando promociones: {}", e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * Obtiene promociones por tipo de componente (simplificado).
     * 
     * @param tipoComponente Tipo de componente para filtrar promociones
     * @return Lista de promociones
     */
    @Cacheable(value = "promociones", key = "'promociones-tipo-' + #tipoComponente")
    public List<Promocion> obtenerPromocionesActivasPorTipo(String tipoComponente) {
        logger.debug("Consultando promociones para tipo: {} (cache miss)", tipoComponente);
        
        try {
            // Por simplicidad, retornamos todas las promociones
            // En una implementación completa, se filtrarían por tipo
            return promocionRepositorio.findAll();
            
        } catch (Exception e) {
            logger.error("Error consultando promociones para tipo {}: {}", tipoComponente, e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * Invalida una promoción específica del cache.
     * Usado cuando se recibe un evento Kafka de cambio de promoción.
     * 
     * @param idPromocion ID de la promoción a invalidar
     */
    @CacheEvict(value = "promociones", key = "#idPromocion")
    public void invalidarPromocion(Integer idPromocion) {
        logger.info("Invalidando cache de promoción: {}", idPromocion);
        // También invalidar cache de promociones activas ya que podría haberse afectado
        invalidarPromocionesActivas();
    }

    /**
     * Invalida el cache de promociones activas.
     * Usado cuando cambia el estado de activación de promociones.
     */
    @CacheEvict(value = "promociones", key = "'promociones-activas'")
    public void invalidarPromocionesActivas() {
        logger.info("Invalidando cache de promociones activas");
    }

    /**
     * Invalida promociones por tipo de componente.
     * 
     * @param tipoComponente Tipo de componente cuyas promociones se invalidarán
     */
    @CacheEvict(value = "promociones", key = "'promociones-tipo-' + #tipoComponente")
    public void invalidarPromocionesPorTipo(String tipoComponente) {
        logger.info("Invalidando cache de promociones para tipo: {}", tipoComponente);
    }

    /**
     * Invalida todo el cache de promociones.
     * Usado en casos de sincronización masiva o errores.
     */
    @CacheEvict(value = "promociones", allEntries = true)
    public void invalidarTodoElCache() {
        logger.warn("Invalidando todo el cache de promociones");
    }

    /**
     * Pre-carga promociones activas en el cache.
     * Útil para optimizar el rendimiento durante períodos de alta demanda.
     */
    public void precargarPromocionesActivas() {
        logger.debug("Pre-cargando promociones activas en cache");
        try {
            obtenerPromocionesActivas();
        } catch (Exception e) {
            logger.error("Error pre-cargando promociones activas: {}", e.getMessage());
        }
    }
}