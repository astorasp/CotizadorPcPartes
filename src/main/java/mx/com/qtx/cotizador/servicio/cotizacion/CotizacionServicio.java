package mx.com.qtx.cotizador.servicio.cotizacion;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mx.com.qtx.cotizador.dominio.core.Cotizacion;
import mx.com.qtx.cotizador.dto.common.response.ApiResponse;
import mx.com.qtx.cotizador.repositorio.ComponenteRepositorio;
import mx.com.qtx.cotizador.repositorio.CotizacionRepositorio;
import mx.com.qtx.cotizador.servicio.wrapper.CotizacionEntityConverter;
import mx.com.qtx.cotizador.util.Errores;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class CotizacionServicio {
    
    private static final Logger logger = LoggerFactory.getLogger(CotizacionServicio.class);
    
    private final CotizacionRepositorio cotizacionRepo;
    private final ComponenteRepositorio componenteRepo;
    
    public CotizacionServicio(CotizacionRepositorio cotizacionRepo, ComponenteRepositorio componenteRepo) {
        this.cotizacionRepo = cotizacionRepo;
        this.componenteRepo = componenteRepo;
    }   

    /**
     * Guarda una cotización en la base de datos
     */
    @Transactional
    public ApiResponse<Void> guardarCotizacion(Cotizacion cotizacion) {
        try {
            if (cotizacion == null) {
                logger.warn("Intento de guardar cotización nula");
                return new ApiResponse<>(Errores.COTIZACION_INVALIDA.getCodigo(), 
                                       Errores.COTIZACION_INVALIDA.getMensaje());
            }

            if (cotizacion.getDetalles() == null || cotizacion.getDetalles().isEmpty()) {
                logger.warn("Intento de guardar cotización sin detalles: {}", cotizacion.getNum());
                return new ApiResponse<>(Errores.COTIZACION_SIN_DETALLES.getCodigo(), 
                                       Errores.COTIZACION_SIN_DETALLES.getMensaje());
            }

            var cotizacionEntity = CotizacionEntityConverter.convertToEntity(cotizacion, null);
            CotizacionEntityConverter.addDetallesTo(cotizacion, cotizacionEntity, componenteRepo);
            cotizacionRepo.save(cotizacionEntity);
            
            logger.info("Cotización guardada exitosamente: {}", cotizacion.getNum());
            return new ApiResponse<>(Errores.OK.getCodigo(), Errores.OK.getMensaje());
            
        } catch (Exception e) {
            logger.error("Error al guardar cotización: {}", e.getMessage(), e);
            return new ApiResponse<>(Errores.ERROR_INTERNO_DEL_SERVICIO.getCodigo(), 
                                   Errores.ERROR_INTERNO_DEL_SERVICIO.getMensaje());
        }
    }

    /**
     * Busca una cotización por su ID
     */
    @Transactional(readOnly = true)
    public ApiResponse<mx.com.qtx.cotizador.entidad.Cotizacion> buscarCotizacionPorId(Integer id) {
        try {
            if (id == null || id <= 0) {
                logger.warn("ID de cotización inválido: {}", id);
                return new ApiResponse<>(Errores.VALOR_INVALIDO.getCodigo(), 
                                       Errores.VALOR_INVALIDO.getMensaje());
            }

            Optional<mx.com.qtx.cotizador.entidad.Cotizacion> cotizacionEntity = cotizacionRepo.findById(id);
            
            if (cotizacionEntity.isEmpty()) {
                logger.warn("Cotización no encontrada con ID: {}", id);
                return new ApiResponse<>(Errores.COTIZACION_NO_ENCONTRADA.getCodigo(), 
                                       Errores.COTIZACION_NO_ENCONTRADA.getMensaje());
            }

            logger.info("Cotización encontrada exitosamente: {}", id);
            return new ApiResponse<>(Errores.OK.getCodigo(), Errores.OK.getMensaje(), cotizacionEntity.get());
            
        } catch (Exception e) {
            logger.error("Error al buscar cotización por ID {}: {}", id, e.getMessage(), e);
            return new ApiResponse<>(Errores.ERROR_INTERNO_DEL_SERVICIO.getCodigo(), 
                                   Errores.ERROR_INTERNO_DEL_SERVICIO.getMensaje());
        }
    }

    /**
     * Lista todas las cotizaciones
     */
    @Transactional(readOnly = true)
    public ApiResponse<List<mx.com.qtx.cotizador.entidad.Cotizacion>> listarCotizaciones() {
        try {
            List<mx.com.qtx.cotizador.entidad.Cotizacion> cotizaciones = cotizacionRepo.findAll();
            
            logger.info("Listado de cotizaciones obtenido exitosamente. Total: {}", cotizaciones.size());
            return new ApiResponse<>(Errores.OK.getCodigo(), Errores.OK.getMensaje(), cotizaciones);
            
        } catch (Exception e) {
            logger.error("Error al listar cotizaciones: {}", e.getMessage(), e);
            return new ApiResponse<>(Errores.ERROR_INTERNO_DEL_SERVICIO.getCodigo(), 
                                   Errores.ERROR_INTERNO_DEL_SERVICIO.getMensaje());
        }
    }

    /**
     * Busca cotizaciones por fecha
     */
    @Transactional(readOnly = true)
    public ApiResponse<List<mx.com.qtx.cotizador.entidad.Cotizacion>> buscarCotizacionesPorFecha(String fecha) {
        try {
            if (fecha == null || fecha.trim().isEmpty()) {
                logger.warn("Fecha de búsqueda vacía o nula");
                return new ApiResponse<>(Errores.VALOR_INVALIDO.getCodigo(), 
                                       "La fecha de búsqueda no puede estar vacía");
            }

            List<mx.com.qtx.cotizador.entidad.Cotizacion> cotizaciones = cotizacionRepo.findByFechaContaining(fecha);
            
            logger.info("Búsqueda por fecha '{}' completada. Encontradas: {}", fecha, cotizaciones.size());
            return new ApiResponse<>(Errores.OK.getCodigo(), Errores.OK.getMensaje(), cotizaciones);
            
        } catch (Exception e) {
            logger.error("Error al buscar cotizaciones por fecha '{}': {}", fecha, e.getMessage(), e);
            return new ApiResponse<>(Errores.ERROR_INTERNO_DEL_SERVICIO.getCodigo(), 
                                   Errores.ERROR_INTERNO_DEL_SERVICIO.getMensaje());
        }
    }

    /**
     * Busca cotizaciones por rango de montos
     */
    @Transactional(readOnly = true)
    public ApiResponse<List<mx.com.qtx.cotizador.entidad.Cotizacion>> buscarCotizacionesPorRangoMonto(BigDecimal montoMin, BigDecimal montoMax) {
        try {
            if (montoMin == null || montoMax == null) {
                logger.warn("Montos de búsqueda nulos: min={}, max={}", montoMin, montoMax);
                return new ApiResponse<>(Errores.VALOR_INVALIDO.getCodigo(), 
                                       "Los montos mínimo y máximo son requeridos");
            }

            if (montoMin.compareTo(BigDecimal.ZERO) < 0 || montoMax.compareTo(BigDecimal.ZERO) < 0) {
                logger.warn("Montos negativos no permitidos: min={}, max={}", montoMin, montoMax);
                return new ApiResponse<>(Errores.MONTO_TOTAL_INVALIDO.getCodigo(), 
                                       Errores.MONTO_TOTAL_INVALIDO.getMensaje());
            }

            if (montoMin.compareTo(montoMax) > 0) {
                logger.warn("Rango de montos inválido: min={} > max={}", montoMin, montoMax);
                return new ApiResponse<>(Errores.RANGO_FECHAS_INVALIDO.getCodigo(), 
                                       "El monto mínimo no puede ser mayor al máximo");
            }

            List<mx.com.qtx.cotizador.entidad.Cotizacion> cotizaciones = cotizacionRepo.findByTotalBetween(montoMin, montoMax);
            
            logger.info("Búsqueda por rango de monto [{} - {}] completada. Encontradas: {}", 
                       montoMin, montoMax, cotizaciones.size());
            return new ApiResponse<>(Errores.OK.getCodigo(), Errores.OK.getMensaje(), cotizaciones);
            
        } catch (Exception e) {
            logger.error("Error al buscar cotizaciones por rango de monto [{} - {}]: {}", 
                        montoMin, montoMax, e.getMessage(), e);
            return new ApiResponse<>(Errores.ERROR_INTERNO_DEL_SERVICIO.getCodigo(), 
                                   Errores.ERROR_INTERNO_DEL_SERVICIO.getMensaje());
        }
    }

    /**
     * Busca cotizaciones por componente
     */
    @Transactional(readOnly = true)
    public ApiResponse<List<mx.com.qtx.cotizador.entidad.Cotizacion>> buscarCotizacionesPorComponente(String idComponente) {
        try {
            if (idComponente == null || idComponente.trim().isEmpty()) {
                logger.warn("ID de componente vacío o nulo");
                return new ApiResponse<>(Errores.VALOR_INVALIDO.getCodigo(), 
                                       "El ID del componente no puede estar vacío");
            }

            List<mx.com.qtx.cotizador.entidad.Cotizacion> cotizaciones = cotizacionRepo.findCotizacionesByComponente(idComponente);
            
            logger.info("Búsqueda por componente '{}' completada. Encontradas: {}", 
                       idComponente, cotizaciones.size());
            return new ApiResponse<>(Errores.OK.getCodigo(), Errores.OK.getMensaje(), cotizaciones);
            
        } catch (Exception e) {
            logger.error("Error al buscar cotizaciones por componente '{}': {}", 
                        idComponente, e.getMessage(), e);
            return new ApiResponse<>(Errores.ERROR_INTERNO_DEL_SERVICIO.getCodigo(), 
                                   Errores.ERROR_INTERNO_DEL_SERVICIO.getMensaje());
        }
    }

    /**
     * Busca cotizaciones con monto mayor al especificado
     */
    @Transactional(readOnly = true)
    public ApiResponse<List<mx.com.qtx.cotizador.entidad.Cotizacion>> buscarCotizacionesConMontoMayorA(BigDecimal montoMinimo) {
        try {
            if (montoMinimo == null) {
                logger.warn("Monto mínimo de búsqueda nulo");
                return new ApiResponse<>(Errores.VALOR_INVALIDO.getCodigo(), 
                                       "El monto mínimo es requerido");
            }

            if (montoMinimo.compareTo(BigDecimal.ZERO) < 0) {
                logger.warn("Monto mínimo negativo no permitido: {}", montoMinimo);
                return new ApiResponse<>(Errores.MONTO_TOTAL_INVALIDO.getCodigo(), 
                                       Errores.MONTO_TOTAL_INVALIDO.getMensaje());
            }

            List<mx.com.qtx.cotizador.entidad.Cotizacion> cotizaciones = cotizacionRepo.findByTotalGreaterThan(montoMinimo);
            
            logger.info("Búsqueda por monto mayor a {} completada. Encontradas: {}", 
                       montoMinimo, cotizaciones.size());
            return new ApiResponse<>(Errores.OK.getCodigo(), Errores.OK.getMensaje(), cotizaciones);
            
        } catch (Exception e) {
            logger.error("Error al buscar cotizaciones con monto mayor a {}: {}", 
                        montoMinimo, e.getMessage(), e);
            return new ApiResponse<>(Errores.ERROR_INTERNO_DEL_SERVICIO.getCodigo(), 
                                   Errores.ERROR_INTERNO_DEL_SERVICIO.getMensaje());
        }
    }

    /**
     * Genera un reporte de resumen de cotizaciones
     */
    @Transactional(readOnly = true)
    public ApiResponse<Map<String, Object>> generarReporteResumen() {
        try {
            List<mx.com.qtx.cotizador.entidad.Cotizacion> todasLasCotizaciones = cotizacionRepo.findAll();
            
            int totalCotizaciones = todasLasCotizaciones.size();
            BigDecimal montoTotalGeneral = todasLasCotizaciones.stream()
                    .map(mx.com.qtx.cotizador.entidad.Cotizacion::getTotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            BigDecimal promedioMonto = totalCotizaciones > 0 ? 
                    montoTotalGeneral.divide(BigDecimal.valueOf(totalCotizaciones)) : BigDecimal.ZERO;
            
            Optional<mx.com.qtx.cotizador.entidad.Cotizacion> cotizacionMayor = todasLasCotizaciones.stream()
                    .max((c1, c2) -> c1.getTotal().compareTo(c2.getTotal()));
            
            Optional<mx.com.qtx.cotizador.entidad.Cotizacion> cotizacionMenor = todasLasCotizaciones.stream()
                    .min((c1, c2) -> c1.getTotal().compareTo(c2.getTotal()));
            
            Map<String, Object> reporte = new java.util.HashMap<>();
            reporte.put("totalCotizaciones", totalCotizaciones);
            reporte.put("montoTotalGeneral", montoTotalGeneral);
            reporte.put("montoPromedio", promedioMonto);
            
            // Agregar cotización mayor si existe
            if (cotizacionMayor.isPresent()) {
                mx.com.qtx.cotizador.entidad.Cotizacion mayor = cotizacionMayor.get();
                reporte.put("cotizacionMayor", java.util.Map.of(
                    "folio", mayor.getFolio(),
                    "total", mayor.getTotal(),
                    "fecha", mayor.getFecha()
                ));
            }
            
            // Agregar cotización menor si existe
            if (cotizacionMenor.isPresent()) {
                mx.com.qtx.cotizador.entidad.Cotizacion menor = cotizacionMenor.get();
                reporte.put("cotizacionMenor", java.util.Map.of(
                    "folio", menor.getFolio(),
                    "total", menor.getTotal(),
                    "fecha", menor.getFecha()
                ));
            }
            
            logger.info("Reporte de resumen generado exitosamente. Total cotizaciones: {}", totalCotizaciones);
            return new ApiResponse<>(Errores.OK.getCodigo(), Errores.OK.getMensaje(), reporte);
            
        } catch (Exception e) {
            logger.error("Error al generar reporte de resumen: {}", e.getMessage(), e);
            return new ApiResponse<>(Errores.ERROR_INTERNO_DEL_SERVICIO.getCodigo(), 
                                   Errores.ERROR_INTERNO_DEL_SERVICIO.getMensaje());
        }
    }
}
