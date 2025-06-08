package mx.com.qtx.cotizador.controlador;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mx.com.qtx.cotizador.dominio.core.componentes.Componente;
import mx.com.qtx.cotizador.dominio.core.componentes.Pc;
import mx.com.qtx.cotizador.dto.common.response.ApiResponse;
import mx.com.qtx.cotizador.dto.pc.mapper.PcMapper;
import mx.com.qtx.cotizador.dto.pc.request.PcCreateRequest;
import mx.com.qtx.cotizador.dto.pc.request.PcUpdateRequest;
import mx.com.qtx.cotizador.dto.pc.request.AgregarComponenteRequest;
import mx.com.qtx.cotizador.dto.pc.response.PcResponse;
import mx.com.qtx.cotizador.dto.componente.response.ComponenteResponse;
import mx.com.qtx.cotizador.dto.componente.mapper.ComponenteMapper;
import mx.com.qtx.cotizador.servicio.componente.ComponenteServicio;
import mx.com.qtx.cotizador.util.HttpStatusMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Controlador REST para la gestión de PCs completas con sub-componentes
 * 
 * Implementa la arquitectura de manejo de errores donde:
 * - Los servicios retornan ApiResponse<T>
 * - El controlador mapea códigos de error a HTTP status
 *   • Código "0" → HTTP 200
 *   • Código "3" → HTTP 500  
 *   • Todo lo demás → HTTP 400
 */
@Slf4j
@RestController
@RequestMapping("/pcs")
@RequiredArgsConstructor
public class PcController {
    
    private final ComponenteServicio componenteServicio;
    
    /**
     * Crear una PC completa con sus sub-componentes
     */
    @PostMapping
    public ResponseEntity<ApiResponse<PcResponse>> crearPc(
            @Valid @RequestBody PcCreateRequest request) {
        
        log.info("Iniciando creación de PC con ID: {}", request.getId());
        
        // Convertir DTO a objeto de dominio
        Pc pc = PcMapper.toPc(request);
        
        // Llamar al servicio especializado para PCs
        ApiResponse<Componente> respuestaServicio = componenteServicio.guardarPcCompleto(pc);
        
        // Mapear el código de error a HTTP status
        HttpStatus httpStatus = HttpStatusMapper.mapearCodigoAHttpStatus(respuestaServicio.getCodigo());
        
        // Convertir a DTO de respuesta si hay datos
        ApiResponse<PcResponse> respuesta;
        if (respuestaServicio.getData() != null && respuestaServicio.getData() instanceof Pc pcResultado) {
            PcResponse pcResponse = PcMapper.toResponse(pcResultado);
            respuesta = new ApiResponse<>(respuestaServicio.getCodigo(), respuestaServicio.getMensaje(), pcResponse);
        } else {
            respuesta = new ApiResponse<>(respuestaServicio.getCodigo(), respuestaServicio.getMensaje());
        }
        
        log.info("Operación completada. Código: {}, HttpStatus: {}", respuestaServicio.getCodigo(), httpStatus);
        return ResponseEntity.status(httpStatus).body(respuesta);
    }
    
    /**
     * Actualizar una PC completa con sus sub-componentes
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PcResponse>> actualizarPc(
            @PathVariable String id,
            @Valid @RequestBody PcUpdateRequest request) {
        
        log.info("Iniciando actualización de PC con ID: {}", id);
        
        // Convertir DTO a objeto de dominio
        Pc pc = PcMapper.toPc(id, request);
        
        // Llamar al servicio especializado para actualizar PCs
        ApiResponse<Componente> respuestaServicio = componenteServicio.actualizarPcCompleto(pc);
        
        // Mapear el código de error a HTTP status
        HttpStatus httpStatus = HttpStatusMapper.mapearCodigoAHttpStatus(respuestaServicio.getCodigo());
        
        // Convertir a DTO de respuesta si hay datos
        ApiResponse<PcResponse> respuesta;
        if (respuestaServicio.getData() != null && respuestaServicio.getData() instanceof Pc pcResultado) {
            PcResponse pcResponse = PcMapper.toResponse(pcResultado);
            respuesta = new ApiResponse<>(respuestaServicio.getCodigo(), respuestaServicio.getMensaje(), pcResponse);
        } else {
            respuesta = new ApiResponse<>(respuestaServicio.getCodigo(), respuestaServicio.getMensaje());
        }
        
        log.info("Operación completada. Código: {}, HttpStatus: {}", respuestaServicio.getCodigo(), httpStatus);
        return ResponseEntity.status(httpStatus).body(respuesta);
    }
    
    /**
     * Obtener una PC por ID con sus sub-componentes
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PcResponse>> obtenerPcPorId(@PathVariable String id) {
        
        log.info("Consultando PC con ID: {}", id);
        
        // Llamar al servicio para buscar la PC
        ApiResponse<Componente> respuestaServicio = componenteServicio.buscarComponente(id);
        
        // Mapear el código de error a HTTP status
        HttpStatus httpStatus = HttpStatusMapper.mapearCodigoAHttpStatus(respuestaServicio.getCodigo());
        
        // Convertir a DTO de respuesta si hay datos y es una PC
        ApiResponse<PcResponse> respuesta;
        if (respuestaServicio.getData() != null) {
            if (respuestaServicio.getData() instanceof Pc pc) {
                PcResponse pcResponse = PcMapper.toResponse(pc);
                respuesta = new ApiResponse<>(respuestaServicio.getCodigo(), respuestaServicio.getMensaje(), pcResponse);
            } else {
                // El componente existe pero no es una PC
                respuesta = new ApiResponse<>("6", "El componente especificado no es una PC");
                httpStatus = HttpStatus.BAD_REQUEST;
            }
        } else {
            respuesta = new ApiResponse<>(respuestaServicio.getCodigo(), respuestaServicio.getMensaje());
        }
        
        log.info("Operación completada. Código: {}, HttpStatus: {}", respuestaServicio.getCodigo(), httpStatus);
        return ResponseEntity.status(httpStatus).body(respuesta);
    }
    
    /**
     * Eliminar una PC completa
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminarPc(@PathVariable String id) {
        
        log.info("Iniciando eliminación de PC con ID: {}", id);
        
        // Primero verificar que sea una PC
        ApiResponse<Componente> verificacion = componenteServicio.buscarComponente(id);
        if (!"0".equals(verificacion.getCodigo())) {
            HttpStatus httpStatus = HttpStatusMapper.mapearCodigoAHttpStatus(verificacion.getCodigo());
            ApiResponse<Void> respuesta = new ApiResponse<>(verificacion.getCodigo(), verificacion.getMensaje());
            return ResponseEntity.status(httpStatus).body(respuesta);
        }
        
        if (!(verificacion.getData() instanceof Pc)) {
            ApiResponse<Void> respuesta = new ApiResponse<>("6", "El componente especificado no es una PC");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(respuesta);
        }
        
        // Eliminar la PC (esto también eliminará las asociaciones en cascada)
        ApiResponse<Void> respuestaServicio = componenteServicio.borrarComponente(id);
        
        // Mapear el código de error a HTTP status
        HttpStatus httpStatus = HttpStatusMapper.mapearCodigoAHttpStatus(respuestaServicio.getCodigo());
        
        log.info("Operación completada. Código: {}, HttpStatus: {}", respuestaServicio.getCodigo(), httpStatus);
        return ResponseEntity.status(httpStatus).body(respuestaServicio);
    }
    
    /**
     * Listar todas las PCs
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<PcResponse>>> obtenerTodasLasPcs() {
        
        log.info("Consultando todas las PCs");
        
        // Obtener todos los componentes y filtrar solo las PCs
        ApiResponse<List<Componente>> respuestaServicio = componenteServicio.buscarPorTipo("PC");
        
        // Mapear el código de error a HTTP status
        HttpStatus httpStatus = HttpStatusMapper.mapearCodigoAHttpStatus(respuestaServicio.getCodigo());
        
        // Convertir a DTOs de respuesta si hay datos
        ApiResponse<List<PcResponse>> respuesta;
        if (respuestaServicio.getData() != null) {
            List<PcResponse> pcsResponse = respuestaServicio.getData().stream()
                    .filter(comp -> comp instanceof Pc)
                    .map(comp -> PcMapper.toResponse((Pc) comp))
                    .collect(Collectors.toList());
            
            respuesta = new ApiResponse<>(respuestaServicio.getCodigo(), 
                                        "PCs obtenidas exitosamente (" + pcsResponse.size() + " encontradas)", 
                                        pcsResponse);
        } else {
            respuesta = new ApiResponse<>(respuestaServicio.getCodigo(), respuestaServicio.getMensaje());
        }
        
        log.info("Operación completada. Código: {}, HttpStatus: {}", respuestaServicio.getCodigo(), httpStatus);
        return ResponseEntity.status(httpStatus).body(respuesta);
    }
    
    /**
     * Caso de uso 2.2: Agregar un componente individual a una PC existente
     */
    @PostMapping("/{pcId}/componentes")
    public ResponseEntity<ApiResponse<ComponenteResponse>> agregarComponenteAPc(
            @PathVariable String pcId,
            @Valid @RequestBody AgregarComponenteRequest request) {
        
        log.info("Agregando componente {} a PC {}", request.getId(), pcId);
        
        // Convertir DTO a objeto de dominio
        Componente componente = PcMapper.toComponente(request);
        
        // Llamar al servicio para agregar el componente a la PC
        ApiResponse<Componente> respuestaServicio = componenteServicio.agregarComponenteAPc(pcId, componente);
        
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
     * Caso de uso 2.3: Quitar un componente específico de una PC
     */
    @DeleteMapping("/{pcId}/componentes/{componenteId}")
    public ResponseEntity<ApiResponse<Void>> quitarComponenteDePc(
            @PathVariable String pcId,
            @PathVariable String componenteId) {
        
        log.info("Quitando componente {} de PC {}", componenteId, pcId);
        
        // Llamar al servicio para quitar el componente de la PC
        ApiResponse<Void> respuestaServicio = componenteServicio.quitarComponenteDePc(pcId, componenteId);
        
        // Mapear el código de error a HTTP status
        HttpStatus httpStatus = HttpStatusMapper.mapearCodigoAHttpStatus(respuestaServicio.getCodigo());
        
        log.info("Operación completada. Código: {}, HttpStatus: {}", respuestaServicio.getCodigo(), httpStatus);
        return ResponseEntity.status(httpStatus).body(respuestaServicio);
    }
    
    /**
     * Listar componentes de una PC específica
     * Endpoint de conveniencia para ver qué componentes tiene una PC
     */
    @GetMapping("/{pcId}/componentes")
    public ResponseEntity<ApiResponse<List<ComponenteResponse>>> listarComponentesDePc(
            @PathVariable String pcId) {
        
        log.info("Consultando componentes de PC {}", pcId);
        
        // Obtener la PC completa con sus componentes
        ApiResponse<Componente> respuestaServicio = componenteServicio.buscarComponente(pcId);
        
        // Mapear el código de error a HTTP status
        HttpStatus httpStatus = HttpStatusMapper.mapearCodigoAHttpStatus(respuestaServicio.getCodigo());
        
        // Extraer solo los sub-componentes si es una PC
        ApiResponse<List<ComponenteResponse>> respuesta;
        if (respuestaServicio.getData() != null && respuestaServicio.getData() instanceof Pc pc) {
            List<ComponenteResponse> componentesResponse = pc.getSubComponentes().stream()
                    .map(ComponenteMapper::toResponse)
                    .collect(Collectors.toList());
            
            respuesta = new ApiResponse<>(respuestaServicio.getCodigo(), 
                                        "Componentes de la PC obtenidos exitosamente (" + componentesResponse.size() + " encontrados)",
                                        componentesResponse);
        } else if (respuestaServicio.getData() != null) {
            // El componente existe pero no es una PC
            respuesta = new ApiResponse<>("6", "El componente especificado no es una PC");
            httpStatus = HttpStatus.BAD_REQUEST;
        } else {
            respuesta = new ApiResponse<>(respuestaServicio.getCodigo(), respuestaServicio.getMensaje());
        }
        
        log.info("Operación completada. Código: {}, HttpStatus: {}", respuestaServicio.getCodigo(), httpStatus);
        return ResponseEntity.status(httpStatus).body(respuesta);
    }
} 