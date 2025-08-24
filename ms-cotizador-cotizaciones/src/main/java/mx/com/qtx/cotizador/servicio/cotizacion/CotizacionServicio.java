package mx.com.qtx.cotizador.servicio.cotizacion;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mx.com.qtx.cotizador.dominio.core.Cotizacion;
import mx.com.qtx.cotizador.dominio.core.ICotizador;
import mx.com.qtx.cotizador.dominio.core.componentes.Componente;
import mx.com.qtx.cotizador.dominio.cotizadorA.Cotizador;
import mx.com.qtx.cotizador.dominio.cotizadorB.CotizadorConMap;
import mx.com.qtx.cotizador.dominio.impuestos.CalculadorImpuesto;
import mx.com.qtx.cotizador.dominio.impuestos.IVA;
import mx.com.qtx.cotizador.dominio.impuestos.CalculadorImpuestoLocal;
import mx.com.qtx.cotizador.dominio.impuestos.CalculadorImpuestoFederal;
import mx.com.qtx.cotizador.dominio.impuestos.CalculadorImpuestoMexico;
import mx.com.qtx.cotizador.dto.common.response.ApiResponse;
import mx.com.qtx.cotizador.dto.cotizacion.request.CotizacionCreateRequest;
import mx.com.qtx.cotizador.dto.cotizacion.request.DetalleCotizacionRequest;
import mx.com.qtx.cotizador.dto.cotizacion.response.CotizacionResponse;
import mx.com.qtx.cotizador.dto.cotizacion.mapper.CotizacionMapper;
import mx.com.qtx.cotizador.dto.componente.response.ComponenteResponse;
import mx.com.qtx.cotizador.repositorio.CotizacionRepositorio;
import mx.com.qtx.cotizador.repositorio.ComponenteRepositorio;
import mx.com.qtx.cotizador.servicio.wrapper.CotizacionEntityConverter;
import org.springframework.beans.factory.annotation.Autowired;
import mx.com.qtx.cotizador.util.Errores;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ArrayList;

@Service
public class CotizacionServicio {
    
    private static final Logger logger = LoggerFactory.getLogger(CotizacionServicio.class);
    
    private final CotizacionRepositorio cotizacionRepo;
    private final ComponenteRepositorio componenteRepositorio;
    
    public CotizacionServicio(CotizacionRepositorio cotizacionRepo, 
                             ComponenteRepositorio componenteRepositorio) {
        this.cotizacionRepo = cotizacionRepo;
        this.componenteRepositorio = componenteRepositorio;
    }   

    /**
     * Guarda una cotización completa usando la lógica de dominio.
     * <p>
     * Este método implementa el flujo arquitectónico correcto:
     * 1. Recibe DTOs de entrada
     * 2. Usa ComponenteServicio para obtener objetos de dominio
     * 3. Aplica lógica de dominio (cotizadores e impuestos)
     * 4. Persiste resultado y retorna DTOs de salida
     * </p>
     * 
     * @param request DTO con los datos para crear la cotización
     * @return ApiResponse con la cotización guardada o error correspondiente
     */
    @Transactional
    public ApiResponse<CotizacionResponse> guardarCotizacion(CotizacionCreateRequest request) {
        try {
            // 1. Validaciones de entrada
            if (request == null) {
                logger.warn("Request de cotización nulo");
                return new ApiResponse<>(Errores.CAMPO_REQUERIDO.getCodigo(), 
                                       "Los datos de cotización son requeridos");
            }
            
            if (request.getDetalles() == null || request.getDetalles().isEmpty()) {
                logger.warn("Request sin detalles");
                return new ApiResponse<>(Errores.COTIZACION_SIN_DETALLES.getCodigo(), 
                                       Errores.COTIZACION_SIN_DETALLES.getMensaje());
            }
            
            // 2. Crear cotizador según tipo especificado
            ICotizador cotizador = crearCotizador(request.getTipoCotizador());
            
            // 3. Agregar componentes al cotizador usando datos locales
            for (DetalleCotizacionRequest detalle : request.getDetalles()) {
                // Buscar componente en la base de datos local
                Optional<mx.com.qtx.cotizador.entidad.Componente> componenteEntity = 
                    componenteRepositorio.findById(detalle.getIdComponente());
                
                if (componenteEntity.isEmpty()) {
                    logger.warn("Componente no encontrado en BD local: {}", detalle.getIdComponente());
                    return new ApiResponse<>(Errores.COMPONENTE_NO_ENCONTRADO_EN_COTIZACION.getCodigo(), 
                                           "Componente no encontrado: " + detalle.getIdComponente());
                }
                
                // Convertir entidad local a objeto de dominio
                Componente compDominio = convertirEntidadADominio(componenteEntity.get());
                
                // Agregar al cotizador
                cotizador.agregarComponente(detalle.getCantidad(), compDominio);
            }
            
            // 4. Generar cotización usando lógica de dominio
            List<CalculadorImpuesto> impuestos = mapearImpuestos(request.getImpuestos());
            Cotizacion cotizacionDominio = cotizador.generarCotizacion(impuestos);
            
            logger.info("Cotización generada con lógica de dominio. Total: {}", cotizacionDominio.getTotal());
            
            // 5. Convertir dominio a entidad JPA para persistir
            mx.com.qtx.cotizador.entidad.Cotizacion cotizacionEntity = 
                CotizacionEntityConverter.convertToNewEntity(cotizacionDominio);
                
            // 6. Persistir la entidad cotización
            mx.com.qtx.cotizador.entidad.Cotizacion cotizacionGuardada = cotizacionRepo.save(cotizacionEntity);
            
            // 8. Convertir a DTO de respuesta
            CotizacionResponse response = CotizacionMapper.toResponse(cotizacionGuardada);
            
            logger.info("Cotización guardada exitosamente con folio: {}", cotizacionGuardada.getFolio());
            return new ApiResponse<>(Errores.OK.getCodigo(), "Cotización guardada exitosamente", response);
            
        } catch (Exception e) {
            logger.error("Error al guardar cotización: {}", e.getMessage(), e);
            return new ApiResponse<>(Errores.ERROR_INTERNO_DEL_SERVICIO.getCodigo(), 
                                   Errores.ERROR_INTERNO_DEL_SERVICIO.getMensaje());
        }
    }
    
    /**
     * Factory para crear cotizador según tipo especificado
     */
    private ICotizador crearCotizador(String tipo) {
        if (tipo == null) {
            tipo = "A"; // Valor por defecto
        }
        
        switch (tipo.toUpperCase()) {
            case "A":
                return new Cotizador(new IVA());
            case "B": 
                return new CotizadorConMap();
            default:
                logger.warn("Tipo de cotizador desconocido '{}', usando tipo A por defecto", tipo);
                return new Cotizador(new IVA());
        }
    }
    
    /**
     * Mapea los tipos de impuestos del DTO a objetos CalculadorImpuesto
     */
    private List<CalculadorImpuesto> mapearImpuestos(List<String> tiposImpuestos) {
        List<CalculadorImpuesto> impuestos = new ArrayList<>();
        
        if (tiposImpuestos == null || tiposImpuestos.isEmpty()) {
            // Aplicar IVA por defecto
            impuestos.add(new IVA());
            return impuestos;
        }
        
        for (String tipo : tiposImpuestos) {
            switch (tipo.toUpperCase()) {
                case "IVA":
                    impuestos.add(new IVA());
                    break;
                case "LOCAL":
                    impuestos.add(new CalculadorImpuestoLocal(new CalculadorImpuestoMexico()));
                    break;
                case "FEDERAL":
                    impuestos.add(new CalculadorImpuestoFederal(new CalculadorImpuestoMexico()));
                    break;
                default:
                    logger.warn("Tipo de impuesto desconocido '{}', se ignora", tipo);
            }
        }
        
        // Si no se mapeó ningún impuesto válido, agregar IVA por defecto
        if (impuestos.isEmpty()) {
            impuestos.add(new IVA());
        }
        
        return impuestos;
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

            var cotizacionEntity = CotizacionEntityConverter.convertToNewEntity(cotizacion);
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
     * Busca una cotización por su ID y la retorna como DTO
     */
    @Transactional(readOnly = true)
    public ApiResponse<CotizacionResponse> buscarCotizacionPorIdComoDTO(Integer id) {
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

            // Convertir entidad a DTO
            CotizacionResponse response = CotizacionMapper.toResponse(cotizacionEntity.get());
            
            logger.info("Cotización encontrada exitosamente: {}", id);
            return new ApiResponse<>(Errores.OK.getCodigo(), Errores.OK.getMensaje(), response);
            
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
     * Lista todas las cotizaciones como DTOs
     */
    @Transactional(readOnly = true)
    public ApiResponse<List<CotizacionResponse>> listarCotizacionesComoDTO() {
        try {
            List<mx.com.qtx.cotizador.entidad.Cotizacion> cotizaciones = cotizacionRepo.findAll();
            
            // Convertir entidades a DTOs
            List<CotizacionResponse> responses = CotizacionMapper.toResponseList(cotizaciones);
            
            logger.info("Listado de cotizaciones obtenido exitosamente. Total: {}", cotizaciones.size());
            return new ApiResponse<>(Errores.OK.getCodigo(), Errores.OK.getMensaje(), responses);
            
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
     * Busca cotizaciones por fecha y las retorna como DTOs
     */
    @Transactional(readOnly = true)
    public ApiResponse<List<CotizacionResponse>> buscarCotizacionesPorFechaComoDTO(String fecha) {
        try {
            if (fecha == null || fecha.trim().isEmpty()) {
                logger.warn("Fecha de búsqueda vacía o nula");
                return new ApiResponse<>(Errores.VALOR_INVALIDO.getCodigo(), 
                                       "La fecha de búsqueda no puede estar vacía");
            }

            List<mx.com.qtx.cotizador.entidad.Cotizacion> cotizaciones = cotizacionRepo.findByFechaContaining(fecha);
            
            // Convertir entidades a DTOs
            List<CotizacionResponse> responses = CotizacionMapper.toResponseList(cotizaciones);
            
            logger.info("Búsqueda por fecha '{}' completada. Encontradas: {}", fecha, cotizaciones.size());
            return new ApiResponse<>(Errores.OK.getCodigo(), Errores.OK.getMensaje(), responses);
            
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
    
    /**
     * Convierte una entidad Componente JPA a objeto de dominio.
     * Reemplaza la funcionalidad que antes hacía ComponenteResponseConverter.
     */
    private Componente convertirEntidadADominio(mx.com.qtx.cotizador.entidad.Componente entidad) {
        if (entidad == null) {
            return null;
        }
        
        // Usar factory methods según el tipo de componente para crear instancias concretas
        String tipoNombre = entidad.getTipoComponente() != null ? 
                           entidad.getTipoComponente().getNombre().toUpperCase() : "MONITOR";
        
        switch (tipoNombre) {
            case "DISCO_DURO":
                return Componente.crearDiscoDuro(
                    entidad.getId(),
                    entidad.getDescripcion() != null ? entidad.getDescripcion() : "Sin descripción",
                    entidad.getMarca() != null ? entidad.getMarca() : "Sin marca",
                    entidad.getModelo() != null ? entidad.getModelo() : "Sin modelo",
                    entidad.getCosto() != null ? entidad.getCosto() : BigDecimal.ZERO,
                    entidad.getPrecioBase() != null ? entidad.getPrecioBase() : BigDecimal.ZERO,
                    entidad.getCapacidadAlm() != null ? entidad.getCapacidadAlm() : "Sin especificar"
                );
                
            case "TARJETA_VIDEO":
                return Componente.crearTarjetaVideo(
                    entidad.getId(),
                    entidad.getDescripcion() != null ? entidad.getDescripcion() : "Sin descripción",
                    entidad.getMarca() != null ? entidad.getMarca() : "Sin marca",
                    entidad.getModelo() != null ? entidad.getModelo() : "Sin modelo",
                    entidad.getCosto() != null ? entidad.getCosto() : BigDecimal.ZERO,
                    entidad.getPrecioBase() != null ? entidad.getPrecioBase() : BigDecimal.ZERO,
                    entidad.getMemoria() != null ? entidad.getMemoria() : "Sin especificar"
                );
                
            case "MONITOR":
            default:
                return Componente.crearMonitor(
                    entidad.getId(),
                    entidad.getDescripcion() != null ? entidad.getDescripcion() : "Sin descripción",
                    entidad.getMarca() != null ? entidad.getMarca() : "Sin marca",
                    entidad.getModelo() != null ? entidad.getModelo() : "Sin modelo",
                    entidad.getCosto() != null ? entidad.getCosto() : BigDecimal.ZERO,
                    entidad.getPrecioBase() != null ? entidad.getPrecioBase() : BigDecimal.ZERO
                );
        }
    }
}
