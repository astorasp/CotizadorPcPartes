package mx.com.qtx.cotizador.controlador;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import mx.com.qtx.cotizador.dto.common.response.ApiResponse;
import mx.com.qtx.cotizador.dto.cotizacion.request.CotizacionCreateRequest;
import mx.com.qtx.cotizador.dto.cotizacion.response.CotizacionResponse;
import mx.com.qtx.cotizador.servicio.cotizacion.CotizacionServicio;
import mx.com.qtx.cotizador.util.HttpStatusMapper;

import java.util.List;

/**
 * Controlador REST para operaciones de cotización.
 * Maneja las peticiones HTTP y delega la lógica de negocio al servicio.
 */
@RestController
@RequestMapping("/cotizaciones")
@CrossOrigin(origins = "*")
public class CotizacionController {
    
    private static final Logger logger = LoggerFactory.getLogger(CotizacionController.class);
    
    private final CotizacionServicio cotizacionServicio;
    
    public CotizacionController(CotizacionServicio cotizacionServicio) {
        this.cotizacionServicio = cotizacionServicio;
    }
    
    /**
     * Crea una nueva cotización usando la lógica de dominio.
     * 
     * @param request DTO con los datos para crear la cotización
     * @return ResponseEntity con la cotización creada o error correspondiente
     */
    @PostMapping
    public ResponseEntity<ApiResponse<CotizacionResponse>> crearCotizacion(
            @Valid @RequestBody CotizacionCreateRequest request) {
        
        logger.info("Creando cotización con tipo: {}, impuestos: {}, detalles: {}", 
                   request.getTipoCotizador(), request.getImpuestos(), request.getDetalles().size());
        
        // Delegar al servicio
        ApiResponse<CotizacionResponse> response = cotizacionServicio.guardarCotizacion(request);
        
        // Mapear código de respuesta a HTTP Status
        return ResponseEntity
                .status(HttpStatusMapper.mapearCodigoAHttpStatus(response.getCodigo()))
                .body(response);
    }
    
    /**
     * Obtiene una cotización por su ID.
     * 
     * @param id ID de la cotización a buscar
     * @return ResponseEntity con la cotización encontrada o error correspondiente
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CotizacionResponse>> obtenerCotizacion(
            @PathVariable Integer id) {
        
        logger.info("Buscando cotización con ID: {}", id);
        
        // Delegar al servicio
        ApiResponse<CotizacionResponse> response = 
            cotizacionServicio.buscarCotizacionPorIdComoDTO(id);
        
        // Mapear código de respuesta a HTTP Status
        return ResponseEntity
                .status(HttpStatusMapper.mapearCodigoAHttpStatus(response.getCodigo()))
                .body(response);
    }
    
    /**
     * Lista todas las cotizaciones.
     * 
     * @return ResponseEntity con la lista de cotizaciones o error correspondiente
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<CotizacionResponse>>> listarCotizaciones() {
        
        logger.info("Listando todas las cotizaciones");
        
        // Delegar al servicio
        ApiResponse<List<CotizacionResponse>> response = 
            cotizacionServicio.listarCotizacionesComoDTO();
        
        // Mapear código de respuesta a HTTP Status
        return ResponseEntity
                .status(HttpStatusMapper.mapearCodigoAHttpStatus(response.getCodigo()))
                .body(response);
    }
    
    /**
     * Busca cotizaciones por fecha.
     * 
     * @param fecha Fecha de búsqueda (formato YYYY-MM-DD)
     * @return ResponseEntity con las cotizaciones encontradas o error correspondiente
     */
    @GetMapping("/buscar/fecha")
    public ResponseEntity<ApiResponse<List<CotizacionResponse>>> buscarPorFecha(
            @RequestParam String fecha) {
        
        logger.info("Buscando cotizaciones por fecha: {}", fecha);
        
        // Delegar al servicio
        ApiResponse<List<CotizacionResponse>> response = 
            cotizacionServicio.buscarCotizacionesPorFechaComoDTO(fecha);
        
        // Mapear código de respuesta a HTTP Status
        return ResponseEntity
                .status(HttpStatusMapper.mapearCodigoAHttpStatus(response.getCodigo()))
                .body(response);
    }
} 