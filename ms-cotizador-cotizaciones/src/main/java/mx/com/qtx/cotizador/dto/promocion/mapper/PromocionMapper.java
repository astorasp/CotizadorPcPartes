package mx.com.qtx.cotizador.dto.promocion.mapper;

import java.util.List;
import java.util.stream.Collectors;

import mx.com.qtx.cotizador.dto.promocion.enums.TipoPromocionAcumulable;
import mx.com.qtx.cotizador.dto.promocion.enums.TipoPromocionBase;
import mx.com.qtx.cotizador.dto.promocion.request.DetallePromocionRequest;
import mx.com.qtx.cotizador.dto.promocion.request.EscalaDescuentoRequest;
import mx.com.qtx.cotizador.dto.promocion.request.PromocionCreateRequest;
import mx.com.qtx.cotizador.dto.promocion.request.PromocionUpdateRequest;
import mx.com.qtx.cotizador.dto.promocion.response.DetallePromocionResponse;
import mx.com.qtx.cotizador.dto.promocion.response.EscalaDescuentoResponse;
import mx.com.qtx.cotizador.dto.promocion.response.PromocionResponse;
import mx.com.qtx.cotizador.entidad.DetallePromDsctoXCant;
import mx.com.qtx.cotizador.entidad.DetallePromocion;
import mx.com.qtx.cotizador.entidad.Promocion;

/**
 * Mapper ultra-complejo para conversiones de promociones.
 * 
 * Maneja todas las conversiones entre:
 * - DTOs ↔ Entidades JPA  
 * - Entidades ↔ Objetos de Dominio
 * - Integración con PromocionBuilder y PromocionEntityConverter
 * 
 * Soporta todos los tipos de promoción:
 * - Base: SIN_DESCUENTO, NXM
 * - Acumulables: DESCUENTO_PLANO, DESCUENTO_POR_CANTIDAD
 */
public class PromocionMapper {

    /**
     * Convierte PromocionCreateRequest a entidad JPA Promocion
     * 
     * @param request DTO de creación
     * @return Entidad JPA lista para persistir
     */
    public static Promocion toEntity(PromocionCreateRequest request) {
        if (request == null) {
            return null;
        }
        
        Promocion entidad = new Promocion();
        entidad.setNombre(request.getNombre());
        entidad.setDescripcion(request.getDescripcion());
        entidad.setVigenciaDesde(request.getVigenciaDesde());
        entidad.setVigenciaHasta(request.getVigenciaHasta());
        
        // Convertir detalles
        if (request.getDetalles() != null) {
            List<DetallePromocion> detallesEntidad = request.getDetalles().stream()
                .map(detalleRequest -> toDetalleEntity(detalleRequest, entidad))
                .collect(Collectors.toList());
            
            entidad.setDetalles(detallesEntidad);
        }
        
        return entidad;
    }
    
    /**
     * Convierte PromocionUpdateRequest a entidad JPA Promocion
     * (Para actualizaciones, mantiene el ID existente)
     * 
     * @param request DTO de actualización
     * @param entidadExistente Entidad existente a actualizar
     * @return Entidad actualizada
     */
    public static Promocion toEntity(PromocionUpdateRequest request, Promocion entidadExistente) {
        if (request == null || entidadExistente == null) {
            return null;
        }
        
        // Actualizar campos básicos
        entidadExistente.setNombre(request.getNombre());
        entidadExistente.setDescripcion(request.getDescripcion());
        entidadExistente.setVigenciaDesde(request.getVigenciaDesde());
        entidadExistente.setVigenciaHasta(request.getVigenciaHasta());
        
        // Limpiar detalles existentes y agregar nuevos
        entidadExistente.getDetalles().clear();
        
        if (request.getDetalles() != null) {
            List<DetallePromocion> detallesActualizados = request.getDetalles().stream()
                .map(detalleRequest -> toDetalleEntity(detalleRequest, entidadExistente))
                .collect(Collectors.toList());
            
            entidadExistente.setDetalles(detallesActualizados);
        }
        
        return entidadExistente;
    }
    
    /**
     * Convierte entidad JPA Promocion a PromocionResponse
     * 
     * @param entidad Entidad JPA
     * @return DTO de respuesta con metadatos calculados
     */
    public static PromocionResponse toResponse(Promocion entidad) {
        if (entidad == null) {
            return null;
        }
        
        // Convertir detalles
        List<DetallePromocionResponse> detallesResponse = null;
        if (entidad.getDetalles() != null) {
            detallesResponse = entidad.getDetalles().stream()
                .map(PromocionMapper::toDetalleResponse)
                .collect(Collectors.toList());
        }
        
        return PromocionResponse.builder()
            .idPromocion(entidad.getIdPromocion())
            .nombre(entidad.getNombre())
            .descripcion(entidad.getDescripcion())
            .vigenciaDesde(entidad.getVigenciaDesde())
            .vigenciaHasta(entidad.getVigenciaHasta())
            .detalles(detallesResponse)
            .build(); // El builder automáticamente calcula metadatos
    }
    
    /**
     * Convierte lista de entidades a lista de responses
     * 
     * @param entidades Lista de entidades
     * @return Lista de DTOs de respuesta
     */
    public static List<PromocionResponse> toResponseList(List<Promocion> entidades) {
        if (entidades == null) {
            return null;
        }
        
        return entidades.stream()
            .map(PromocionMapper::toResponse)
            .collect(Collectors.toList());
    }
    
    /**
     * Convierte DetallePromocionRequest a entidad DetallePromocion
     * 
     * @param request DTO del detalle
     * @param promocion Promoción padre
     * @return Entidad del detalle
     */
    private static DetallePromocion toDetalleEntity(DetallePromocionRequest request, Promocion promocion) {
        if (request == null) {
            return null;
        }
        
        DetallePromocion detalle = new DetallePromocion();
        detalle.setNombre(request.getNombre());
        detalle.setEsBase(request.getEsBase());
        detalle.setPromocion(promocion);
        
        if (request.getEsBase() != null && request.getEsBase()) {
            // Configurar promoción base
            configurarDetalleBase(detalle, request);
        } else {
            // Configurar promoción acumulable
            configurarDetalleAcumulable(detalle, request);
        }
        
        return detalle;
    }
    
    /**
     * Configura un detalle como promoción base
     */
    private static void configurarDetalleBase(DetallePromocion detalle, DetallePromocionRequest request) {
        if (request.getTipoBase() == null) {
            return;
        }
        
        detalle.setTipoPromBase(request.getTipoBase().getCodigo());
        
        switch (request.getTipoBase()) {
            case SIN_DESCUENTO:
                // Sin parámetros adicionales
                detalle.setLlevent(0);
                detalle.setPaguen(0);
                detalle.setPorcDctoPlano(0.0);
                break;
                
            case NXM:
                if (request.getParametrosNxM() != null) {
                    detalle.setLlevent(request.getParametrosNxM().getLlevent());
                    detalle.setPaguen(request.getParametrosNxM().getPaguen());
                } else {
                    detalle.setLlevent(0);
                    detalle.setPaguen(0);
                }
                detalle.setPorcDctoPlano(0.0);
                break;
        }
    }
    
    /**
     * Configura un detalle como promoción acumulable
     */
    private static void configurarDetalleAcumulable(DetallePromocion detalle, DetallePromocionRequest request) {
        if (request.getTipoAcumulable() == null) {
            return;
        }
        
        detalle.setTipoPromAcumulable(request.getTipoAcumulable().getCodigo());
        
        switch (request.getTipoAcumulable()) {
            case DESCUENTO_PLANO:
                if (request.getPorcentajeDescuentoPlano() != null) {
                    detalle.setPorcDctoPlano(request.getPorcentajeDescuentoPlano());
                } else {
                    detalle.setPorcDctoPlano(0.0);
                }
                detalle.setLlevent(0);
                detalle.setPaguen(0);
                break;
                
            case DESCUENTO_POR_CANTIDAD:
                detalle.setPorcDctoPlano(0.0);
                detalle.setLlevent(0);
                detalle.setPaguen(0);
                
                // Convertir escalas de descuento
                if (request.getEscalasDescuento() != null) {
                    List<DetallePromDsctoXCant> escalasEntidad = request.getEscalasDescuento().stream()
                        .map(escala -> toEscalaEntity(escala, detalle))
                        .collect(Collectors.toList());
                    
                    detalle.setDescuentosPorCantidad(escalasEntidad);
                }
                break;
        }
    }
    
    /**
     * Convierte EscalaDescuentoRequest a DetallePromDsctoXCant
     */
    private static DetallePromDsctoXCant toEscalaEntity(EscalaDescuentoRequest request, DetallePromocion detalle) {
        if (request == null) {
            return null;
        }
        
        DetallePromDsctoXCant escala = new DetallePromDsctoXCant();
        escala.setCantidad(request.getCantidad());
        escala.setDscto(request.getDescuento());
        escala.setDetallePromocion(detalle);
        
        return escala;
    }
    
    /**
     * Convierte entidad DetallePromocion a DetallePromocionResponse
     */
    private static DetallePromocionResponse toDetalleResponse(DetallePromocion entidad) {
        if (entidad == null) {
            return null;
        }
        
        DetallePromocionResponse response = new DetallePromocionResponse();
        response.setIdDetalle(entidad.getIdDetallePromocion());
        response.setNombre(entidad.getNombre());
        response.setEsBase(entidad.getEsBase());
        
        if (entidad.getEsBase() != null && entidad.getEsBase()) {
            // Configurar respuesta para promoción base
            configurarResponseBase(response, entidad);
        } else {
            // Configurar respuesta para promoción acumulable
            configurarResponseAcumulable(response, entidad);
        }
        
        // Calcular descripción del tipo
        response.calcularDescripcionTipo();
        
        return response;
    }
    
    /**
     * Configura response para promoción base
     */
    private static void configurarResponseBase(DetallePromocionResponse response, DetallePromocion entidad) {
        if (entidad.getTipoPromBase() != null) {
            try {
                response.setTipoBase(TipoPromocionBase.fromCodigo(entidad.getTipoPromBase()));
                
                if (response.getTipoBase() == TipoPromocionBase.NXM) {
                    response.setLlevent(entidad.getLlevent());
                    response.setPaguen(entidad.getPaguen());
                }
            } catch (IllegalArgumentException e) {
                // Tipo no reconocido, se maneja en calcularDescripcionTipo()
            }
        }
    }
    
    /**
     * Configura response para promoción acumulable
     */
    private static void configurarResponseAcumulable(DetallePromocionResponse response, DetallePromocion entidad) {
        if (entidad.getTipoPromAcumulable() != null) {
            try {
                response.setTipoAcumulable(TipoPromocionAcumulable.fromCodigo(entidad.getTipoPromAcumulable()));
                
                switch (response.getTipoAcumulable()) {
                    case DESCUENTO_PLANO:
                        response.setPorcentajeDescuentoPlano(entidad.getPorcDctoPlano());
                        break;
                        
                    case DESCUENTO_POR_CANTIDAD:
                        if (entidad.getDescuentosPorCantidad() != null) {
                            List<EscalaDescuentoResponse> escalasResponse = entidad.getDescuentosPorCantidad().stream()
                                .map(PromocionMapper::toEscalaResponse)
                                .collect(Collectors.toList());
                            
                            response.setEscalasDescuento(escalasResponse);
                        }
                        break;
                }
            } catch (IllegalArgumentException e) {
                // Tipo no reconocido, se maneja en calcularDescripcionTipo()
            }
        }
    }
    
    /**
     * Convierte DetallePromDsctoXCant a EscalaDescuentoResponse
     */
    private static EscalaDescuentoResponse toEscalaResponse(DetallePromDsctoXCant entidad) {
        if (entidad == null) {
            return null;
        }
        
        return new EscalaDescuentoResponse(entidad.getCantidad(), entidad.getDscto());
    }
    
    // =================================================================
    // MÉTODOS DE CONVERSIÓN A DOMINIO DE PROMOCIONES
    // =================================================================
    
    /**
     * Convierte una entidad JPA Promocion a objeto de dominio IPromocion.
     * Utiliza PromocionBuilder para crear el tipo específico de promoción.
     * 
     * @param entidad Entidad JPA de promoción
     * @return Objeto de dominio que implementa IPromocion
     */
    public static mx.com.qtx.cotizador.dominio.core.componentes.IPromocion toDominio(Promocion entidad) {
        return mx.com.qtx.cotizador.servicio.wrapper.PromocionEntityConverter.convertirADominio(entidad);
    }
    
    /**
     * Convierte una entidad JPA Promocion a objeto de dominio con información de debugging.
     * 
     * @param entidad Entidad JPA de promoción
     * @param debug Si debe imprimir información de debugging
     * @return Objeto de dominio que implementa IPromocion
     */
    public static mx.com.qtx.cotizador.dominio.core.componentes.IPromocion toDominioConDebug(Promocion entidad, boolean debug) {
        return mx.com.qtx.cotizador.servicio.wrapper.PromocionEntityConverter.convertirADominioConDebug(entidad, debug);
    }
    
    /**
     * Método helper para obtener información sobre el tipo de promoción
     * sin hacer la conversión completa.
     * 
     * @param entidad Entidad a analizar
     * @return String con información sobre el tipo de promoción
     */
    public static String analizarTipoPromocion(Promocion entidad) {
        return mx.com.qtx.cotizador.servicio.wrapper.PromocionEntityConverter.obtenerInfoTipoPromocion(entidad);
    }
    
    /**
     * Verifica si una entidad de promoción puede ser convertida exitosamente a dominio.
     * 
     * @param entidad Entidad a validar
     * @return true si la conversión será exitosa
     */
    public static boolean puedeConvertirADominio(Promocion entidad) {
        return mx.com.qtx.cotizador.servicio.wrapper.PromocionEntityConverter.puedeConvertir(entidad);
    }
} 