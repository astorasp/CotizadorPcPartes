package mx.com.qtx.cotizador.controlador;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mx.com.qtx.cotizador.dominio.core.componentes.Componente;
import mx.com.qtx.cotizador.dto.common.response.ApiResponse;
import mx.com.qtx.cotizador.dto.componente.mapper.ComponenteMapper;
import mx.com.qtx.cotizador.dto.componente.request.ComponenteCreateRequest;
import mx.com.qtx.cotizador.dto.componente.request.ComponenteUpdateRequest;
import mx.com.qtx.cotizador.dto.componente.response.ComponenteResponse;
import mx.com.qtx.cotizador.servicio.componente.ComponenteServicio;
import mx.com.qtx.cotizador.util.HttpStatusMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Controlador REST para la gestión de componentes
 * 
 * Implementa la nueva arquitectura donde:
 * - Los servicios retornan ApiResponse<T>
 * - El controlador mapea códigos de error a HTTP status sin condicionales innecesarias
 *   • Código "0" → HTTP 200
 *   • Código "3" → HTTP 500  
 *   • Todo lo demás → HTTP 400
 */
@Slf4j
@RestController
@RequestMapping("/componentes")
@RequiredArgsConstructor
public class ComponenteController {
    
    private final ComponenteServicio componenteServicio;
    
    /**
     * Caso de uso 1.1: Agregar componente
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ComponenteResponse>> crearComponente(
            @Valid @RequestBody ComponenteCreateRequest request) {
        
        log.info("Iniciando creación de componente con ID: {}", request.getId());
        
        // Convertir DTO a objeto de dominio
        Componente componente = ComponenteMapper.toComponente(request);
        
        // Llamar al servicio y obtener ApiResponse
        ApiResponse<Componente> respuestaServicio = componenteServicio.guardarComponente(componente);
        
        // Mapear el código de error a HTTP status
        HttpStatus httpStatus = HttpStatusMapper.mapearCodigoAHttpStatus(respuestaServicio.getCodigo());
        
        // Convertir a DTO de respuesta si hay datos
        ApiResponse<ComponenteResponse> respuesta;
        if (respuestaServicio.getData() != null) {
            ComponenteResponse componenteResponse = ComponenteMapper.toResponse(respuestaServicio.getData());
            respuesta = new ApiResponse<>(respuestaServicio.getCodigo(), respuestaServicio.getMensaje(), componenteResponse);
        } else {
            respuesta = new ApiResponse<>(respuestaServicio.getCodigo(), respuestaServicio.getMensaje());
        }
        
        log.info("Operación completada. Código: {}, HttpStatus: {}", respuestaServicio.getCodigo(), httpStatus);
        return ResponseEntity.status(httpStatus).body(respuesta);
    }
    
    /**
     * Caso de uso 1.2: Modificar componente
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ComponenteResponse>> modificarComponente(
            @PathVariable String id,
            @Valid @RequestBody ComponenteUpdateRequest request) {
        
        log.info("Iniciando actualización de componente con ID: {}", id);
        
        // Convertir DTO a objeto de dominio
        Componente componente = ComponenteMapper.toComponente(id, request);
        
        // Llamar al servicio y obtener ApiResponse
        ApiResponse<Componente> respuestaServicio = componenteServicio.actualizarComponente(componente);
        
        // Mapear el código de error a HTTP status
        HttpStatus httpStatus = HttpStatusMapper.mapearCodigoAHttpStatus(respuestaServicio.getCodigo());
        
        // Convertir a DTO de respuesta si hay datos
        ApiResponse<ComponenteResponse> respuesta;
        if (respuestaServicio.getData() != null) {
            ComponenteResponse componenteResponse = ComponenteMapper.toResponse(respuestaServicio.getData());
            respuesta = new ApiResponse<>(respuestaServicio.getCodigo(), respuestaServicio.getMensaje(), componenteResponse);
        } else {
            respuesta = new ApiResponse<>(respuestaServicio.getCodigo(), respuestaServicio.getMensaje());
        }
        
        log.info("Operación completada. Código: {}, HttpStatus: {}", respuestaServicio.getCodigo(), httpStatus);
        return ResponseEntity.status(httpStatus).body(respuesta);
    }
    
    /**
     * Caso de uso 1.3: Eliminar componente
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminarComponente(@PathVariable String id) {
        
        log.info("Iniciando eliminación de componente con ID: {}", id);
        
        // Llamar al servicio y obtener ApiResponse
        ApiResponse<Void> respuestaServicio = componenteServicio.borrarComponente(id);
        
        // Mapear el código de error a HTTP status
        HttpStatus httpStatus = HttpStatusMapper.mapearCodigoAHttpStatus(respuestaServicio.getCodigo());
        
        log.info("Operación completada. Código: {}, HttpStatus: {}", respuestaServicio.getCodigo(), httpStatus);
        return ResponseEntity.status(httpStatus).body(respuestaServicio);
    }
    
    /**
     * Caso de uso 1.4: Consultar componentes - Obtener todos
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ComponenteResponse>>> obtenerTodosLosComponentes() {
        
        log.info("Consultando todos los componentes");
        
        // Llamar al servicio y obtener ApiResponse
        ApiResponse<List<Componente>> respuestaServicio = componenteServicio.obtenerTodosLosComponentes();
        
        // Mapear el código de error a HTTP status
        HttpStatus httpStatus = HttpStatusMapper.mapearCodigoAHttpStatus(respuestaServicio.getCodigo());
        
        // Convertir a DTOs de respuesta si hay datos
        ApiResponse<List<ComponenteResponse>> respuesta;
        if (respuestaServicio.getData() != null) {
            List<ComponenteResponse> componentesResponse = respuestaServicio.getData().stream()
                    .map(ComponenteMapper::toResponse)
                    .collect(Collectors.toList());
            respuesta = new ApiResponse<>(respuestaServicio.getCodigo(), respuestaServicio.getMensaje(), componentesResponse);
        } else {
            respuesta = new ApiResponse<>(respuestaServicio.getCodigo(), respuestaServicio.getMensaje());
        }
        
        log.info("Operación completada. Código: {}, HttpStatus: {}", respuestaServicio.getCodigo(), httpStatus);
        return ResponseEntity.status(httpStatus).body(respuesta);
    }
    
    /**
     * Caso de uso 1.4: Consultar componentes - Obtener por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ComponenteResponse>> obtenerComponentePorId(@PathVariable String id) {
        
        log.info("Consultando componente con ID: {}", id);
        
        // Llamar al servicio y obtener ApiResponse
        ApiResponse<Componente> respuestaServicio = componenteServicio.buscarComponente(id);
        
        // Mapear el código de error a HTTP status
        HttpStatus httpStatus = HttpStatusMapper.mapearCodigoAHttpStatus(respuestaServicio.getCodigo());
        
        // Convertir a DTO de respuesta si hay datos
        ApiResponse<ComponenteResponse> respuesta;
        if (respuestaServicio.getData() != null) {
            ComponenteResponse componenteResponse = ComponenteMapper.toResponse(respuestaServicio.getData());
            respuesta = new ApiResponse<>(respuestaServicio.getCodigo(), respuestaServicio.getMensaje(), componenteResponse);
        } else {
            respuesta = new ApiResponse<>(respuestaServicio.getCodigo(), respuestaServicio.getMensaje());
        }
        
        log.info("Operación completada. Código: {}, HttpStatus: {}", respuestaServicio.getCodigo(), httpStatus);
        return ResponseEntity.status(httpStatus).body(respuesta);
    }
    
    /**
     * Caso de uso 1.4: Consultar componentes - Filtrar por tipo
     */
    @GetMapping("/tipo/{tipo}")
    public ResponseEntity<ApiResponse<List<ComponenteResponse>>> obtenerComponentesPorTipo(@PathVariable String tipo) {
        
        log.info("Consultando componentes por tipo: {}", tipo);
        
        // Llamar al servicio y obtener ApiResponse
        ApiResponse<List<Componente>> respuestaServicio = componenteServicio.buscarPorTipo(tipo);
        
        // Mapear el código de error a HTTP status
        HttpStatus httpStatus = HttpStatusMapper.mapearCodigoAHttpStatus(respuestaServicio.getCodigo());
        
        // Convertir a DTOs de respuesta si hay datos
        ApiResponse<List<ComponenteResponse>> respuesta;
        if (respuestaServicio.getData() != null) {
            List<ComponenteResponse> componentesResponse = respuestaServicio.getData().stream()
                    .map(ComponenteMapper::toResponse)
                    .collect(Collectors.toList());
            respuesta = new ApiResponse<>(respuestaServicio.getCodigo(), respuestaServicio.getMensaje(), componentesResponse);
        } else {
            respuesta = new ApiResponse<>(respuestaServicio.getCodigo(), respuestaServicio.getMensaje());
        }
        
        log.info("Operación completada. Código: {}, HttpStatus: {}", respuestaServicio.getCodigo(), httpStatus);
        return ResponseEntity.status(httpStatus).body(respuesta);
    }
} 