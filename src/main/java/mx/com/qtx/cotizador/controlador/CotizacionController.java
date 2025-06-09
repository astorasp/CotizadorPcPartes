package mx.com.qtx.cotizador.controlador;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mx.com.qtx.cotizador.dto.common.response.ApiResponse;
import mx.com.qtx.cotizador.dto.cotizacion.request.CotizacionCreateRequest;
import mx.com.qtx.cotizador.dto.cotizacion.response.CotizacionResponse;
import mx.com.qtx.cotizador.dto.cotizacion.mapper.CotizacionMapper;
import mx.com.qtx.cotizador.entidad.Cotizacion;
import mx.com.qtx.cotizador.servicio.cotizacion.CotizacionServicio;
import mx.com.qtx.cotizador.util.HttpStatusMapper;
import mx.com.qtx.cotizador.util.Errores;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Controlador REST para la gestión de cotizaciones.
 * Proporciona endpoints para crear, consultar y generar reportes de cotizaciones.
 */
@RestController
@RequestMapping("/cotizaciones")
@Validated
public class CotizacionController {
    
    private static final Logger logger = LoggerFactory.getLogger(CotizacionController.class);
    
    private final CotizacionServicio cotizacionServicio;
    
    public CotizacionController(CotizacionServicio cotizacionServicio) {
        this.cotizacionServicio = cotizacionServicio;
    }
    
    /**
     * Crea una nueva cotización
     * 
     * @param request Datos de la cotización a crear
     * @return Respuesta con el resultado de la operación
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> crearCotizacion(@Valid @RequestBody CotizacionCreateRequest request) {
        logger.info("Solicitud para crear nueva cotización con {} detalles", 
                   request.getDetalles() != null ? request.getDetalles().size() : 0);
        
        // Convertir request a dominio
        mx.com.qtx.cotizador.dominio.core.Cotizacion cotizacionDomain = 
                CotizacionMapper.toDomain(request, cotizacionServicio);
        
        // Guardar cotización
        ApiResponse<Void> response = cotizacionServicio.guardarCotizacion(cotizacionDomain);
        
        return ResponseEntity
                .status(HttpStatusMapper.mapearCodigoAHttpStatus(response.getCodigo()))
                .body(response);
    }
    
    /**
     * Obtiene una cotización por su ID
     * 
     * @param id ID de la cotización
     * @return Cotización encontrada
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CotizacionResponse>> obtenerCotizacion(
            @PathVariable @Positive(message = "El ID debe ser positivo") Integer id) {
        
        logger.info("Solicitud para obtener cotización con ID: {}", id);
        
        ApiResponse<Cotizacion> serviceResponse = cotizacionServicio.buscarCotizacionPorId(id);
        
        if (Errores.OK.getCodigo().equals(serviceResponse.getCodigo())) {
            CotizacionResponse cotizacionResponse = CotizacionMapper.toResponse(serviceResponse.getData());
            ApiResponse<CotizacionResponse> response = new ApiResponse<>(
                    serviceResponse.getCodigo(),
                    serviceResponse.getMensaje(),
                    cotizacionResponse
            );
            
            return ResponseEntity
                    .status(HttpStatusMapper.mapearCodigoAHttpStatus(response.getCodigo()))
                    .body(response);
        } else {
            ApiResponse<CotizacionResponse> response = new ApiResponse<>(
                    serviceResponse.getCodigo(),
                    serviceResponse.getMensaje()
            );
            
            return ResponseEntity
                    .status(HttpStatusMapper.mapearCodigoAHttpStatus(response.getCodigo()))
                    .body(response);
        }
    }
    
    /**
     * Lista todas las cotizaciones
     * 
     * @return Lista de todas las cotizaciones
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<CotizacionResponse>>> listarCotizaciones() {
        logger.info("Solicitud para listar todas las cotizaciones");
        
        ApiResponse<List<Cotizacion>> serviceResponse = cotizacionServicio.listarCotizaciones();
        
        if (Errores.OK.getCodigo().equals(serviceResponse.getCodigo())) {
            List<CotizacionResponse> cotizacionesResponse = CotizacionMapper.toResponseList(serviceResponse.getData());
            ApiResponse<List<CotizacionResponse>> response = new ApiResponse<>(
                    serviceResponse.getCodigo(),
                    serviceResponse.getMensaje(),
                    cotizacionesResponse
            );
            
            return ResponseEntity
                    .status(HttpStatusMapper.mapearCodigoAHttpStatus(response.getCodigo()))
                    .body(response);
        } else {
            ApiResponse<List<CotizacionResponse>> response = new ApiResponse<>(
                    serviceResponse.getCodigo(),
                    serviceResponse.getMensaje()
            );
            
            return ResponseEntity
                    .status(HttpStatusMapper.mapearCodigoAHttpStatus(response.getCodigo()))
                    .body(response);
        }
    }
    
    /**
     * Busca cotizaciones por fecha
     * 
     * @param fecha Fecha a buscar (formato: yyyy-MM-dd o parte de la fecha)
     * @return Lista de cotizaciones que coinciden con la fecha
     */
    @GetMapping("/buscar/fecha")
    public ResponseEntity<ApiResponse<List<CotizacionResponse>>> buscarPorFecha(
            @RequestParam @NotBlank(message = "La fecha no puede estar vacía") String fecha) {
        
        logger.info("Solicitud para buscar cotizaciones por fecha: {}", fecha);
        
        ApiResponse<List<Cotizacion>> serviceResponse = cotizacionServicio.buscarCotizacionesPorFecha(fecha);
        
        if (Errores.OK.getCodigo().equals(serviceResponse.getCodigo())) {
            List<CotizacionResponse> cotizacionesResponse = CotizacionMapper.toResponseList(serviceResponse.getData());
            ApiResponse<List<CotizacionResponse>> response = new ApiResponse<>(
                    serviceResponse.getCodigo(),
                    serviceResponse.getMensaje(),
                    cotizacionesResponse
            );
            
            return ResponseEntity
                    .status(HttpStatusMapper.mapearCodigoAHttpStatus(response.getCodigo()))
                    .body(response);
        } else {
            ApiResponse<List<CotizacionResponse>> response = new ApiResponse<>(
                    serviceResponse.getCodigo(),
                    serviceResponse.getMensaje()
            );
            
            return ResponseEntity
                    .status(HttpStatusMapper.mapearCodigoAHttpStatus(response.getCodigo()))
                    .body(response);
        }
    }
    
    /**
     * Busca cotizaciones por rango de montos
     * 
     * @param montoMin Monto mínimo
     * @param montoMax Monto máximo
     * @return Lista de cotizaciones dentro del rango
     */
    @GetMapping("/buscar/rango-monto")
    public ResponseEntity<ApiResponse<List<CotizacionResponse>>> buscarPorRangoMonto(
            @RequestParam @Positive(message = "El monto mínimo debe ser positivo") BigDecimal montoMin,
            @RequestParam @Positive(message = "El monto máximo debe ser positivo") BigDecimal montoMax) {
        
        logger.info("Solicitud para buscar cotizaciones por rango de monto: {} - {}", montoMin, montoMax);
        
        ApiResponse<List<Cotizacion>> serviceResponse = cotizacionServicio.buscarCotizacionesPorRangoMonto(montoMin, montoMax);
        
        if (Errores.OK.getCodigo().equals(serviceResponse.getCodigo())) {
            List<CotizacionResponse> cotizacionesResponse = CotizacionMapper.toResponseList(serviceResponse.getData());
            ApiResponse<List<CotizacionResponse>> response = new ApiResponse<>(
                    serviceResponse.getCodigo(),
                    serviceResponse.getMensaje(),
                    cotizacionesResponse
            );
            
            return ResponseEntity
                    .status(HttpStatusMapper.mapearCodigoAHttpStatus(response.getCodigo()))
                    .body(response);
        } else {
            ApiResponse<List<CotizacionResponse>> response = new ApiResponse<>(
                    serviceResponse.getCodigo(),
                    serviceResponse.getMensaje()
            );
            
            return ResponseEntity
                    .status(HttpStatusMapper.mapearCodigoAHttpStatus(response.getCodigo()))
                    .body(response);
        }
    }
    
    /**
     * Busca cotizaciones que contienen un componente específico
     * 
     * @param idComponente ID del componente a buscar
     * @return Lista de cotizaciones que contienen el componente
     */
    @GetMapping("/buscar/componente")
    public ResponseEntity<ApiResponse<List<CotizacionResponse>>> buscarPorComponente(
            @RequestParam @NotBlank(message = "El ID del componente no puede estar vacío") String idComponente) {
        
        logger.info("Solicitud para buscar cotizaciones por componente: {}", idComponente);
        
        ApiResponse<List<Cotizacion>> serviceResponse = cotizacionServicio.buscarCotizacionesPorComponente(idComponente);
        
        if (Errores.OK.getCodigo().equals(serviceResponse.getCodigo())) {
            List<CotizacionResponse> cotizacionesResponse = CotizacionMapper.toResponseList(serviceResponse.getData());
            ApiResponse<List<CotizacionResponse>> response = new ApiResponse<>(
                    serviceResponse.getCodigo(),
                    serviceResponse.getMensaje(),
                    cotizacionesResponse
            );
            
            return ResponseEntity
                    .status(HttpStatusMapper.mapearCodigoAHttpStatus(response.getCodigo()))
                    .body(response);
        } else {
            ApiResponse<List<CotizacionResponse>> response = new ApiResponse<>(
                    serviceResponse.getCodigo(),
                    serviceResponse.getMensaje()
            );
            
            return ResponseEntity
                    .status(HttpStatusMapper.mapearCodigoAHttpStatus(response.getCodigo()))
                    .body(response);
        }
    }
    
    /**
     * Busca cotizaciones con monto mayor al especificado
     * 
     * @param montoMinimo Monto mínimo
     * @return Lista de cotizaciones con monto mayor al especificado
     */
    @GetMapping("/buscar/monto-mayor")
    public ResponseEntity<ApiResponse<List<CotizacionResponse>>> buscarConMontoMayorA(
            @RequestParam @Positive(message = "El monto mínimo debe ser positivo") BigDecimal montoMinimo) {
        
        logger.info("Solicitud para buscar cotizaciones con monto mayor a: {}", montoMinimo);
        
        ApiResponse<List<Cotizacion>> serviceResponse = cotizacionServicio.buscarCotizacionesConMontoMayorA(montoMinimo);
        
        if (Errores.OK.getCodigo().equals(serviceResponse.getCodigo())) {
            List<CotizacionResponse> cotizacionesResponse = CotizacionMapper.toResponseList(serviceResponse.getData());
            ApiResponse<List<CotizacionResponse>> response = new ApiResponse<>(
                    serviceResponse.getCodigo(),
                    serviceResponse.getMensaje(),
                    cotizacionesResponse
            );
            
            return ResponseEntity
                    .status(HttpStatusMapper.mapearCodigoAHttpStatus(response.getCodigo()))
                    .body(response);
        } else {
            ApiResponse<List<CotizacionResponse>> response = new ApiResponse<>(
                    serviceResponse.getCodigo(),
                    serviceResponse.getMensaje()
            );
            
            return ResponseEntity
                    .status(HttpStatusMapper.mapearCodigoAHttpStatus(response.getCodigo()))
                    .body(response);
        }
    }
    
    /**
     * Genera un reporte de resumen de cotizaciones
     * 
     * @return Reporte con estadísticas de cotizaciones
     */
    @GetMapping("/reporte/resumen")
    public ResponseEntity<ApiResponse<Map<String, Object>>> generarReporteResumen() {
        logger.info("Solicitud para generar reporte de resumen de cotizaciones");
        
        ApiResponse<Map<String, Object>> response = cotizacionServicio.generarReporteResumen();
        
        return ResponseEntity
                .status(HttpStatusMapper.mapearCodigoAHttpStatus(response.getCodigo()))
                .body(response);
    }
} 