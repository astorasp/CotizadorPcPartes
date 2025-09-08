package mx.com.qtx.cotizador.dto.promocion.mapper;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import mx.com.qtx.cotizador.dto.promocion.enums.TipoPromocionAcumulable;
import mx.com.qtx.cotizador.dto.promocion.enums.TipoPromocionBase;
import mx.com.qtx.cotizador.dto.promocion.request.DetallePromocionRequest;
import mx.com.qtx.cotizador.dto.promocion.request.EscalaDescuentoRequest;
import mx.com.qtx.cotizador.dto.promocion.request.ParametrosNxMRequest;
import mx.com.qtx.cotizador.dto.promocion.request.PromocionCreateRequest;
import mx.com.qtx.cotizador.dto.promocion.request.PromocionUpdateRequest;
import mx.com.qtx.cotizador.dto.promocion.response.DetallePromocionResponse;
import mx.com.qtx.cotizador.dto.promocion.response.EscalaDescuentoResponse;
import mx.com.qtx.cotizador.dto.promocion.response.ParametrosNxMResponse;
import mx.com.qtx.cotizador.dto.promocion.response.PromocionResponse;
import mx.com.qtx.cotizador.dominio.promos.PromAcumulable;
import mx.com.qtx.cotizador.dominio.promos.PromNXM;

/**
 * Mapper ultra-complejo para conversiones de promociones.
 * 
 * Maneja conversiones entre:
 * - DTOs ↔ Objetos de Dominio
 * 
 * Soporta todos los tipos de promoción:
 * - Base: SIN_DESCUENTO, NXM
 * - Acumulables: DESCUENTO_PLANO, DESCUENTO_POR_CANTIDAD
 */
public class PromocionMapper {

    /**
     * Convierte un DTO de creación a objeto de dominio, construyendo la cadena base + acumulables.
     */
    public static mx.com.qtx.cotizador.dominio.promos.Promocion toDominio(PromocionCreateRequest request) {
        if (request == null) {
            return null;
        }

        mx.com.qtx.cotizador.dominio.promos.Promocion promocion = construirDominioDesdeDetalles(
            request.getDetalles(), request.getNombre(), request.getDescripcion(), null);
        return promocion;
    }

    /**
     * Convierte un DTO de actualización a objeto de dominio, construyendo la cadena base + acumulables.
     */
    public static mx.com.qtx.cotizador.dominio.promos.Promocion toDominio(PromocionUpdateRequest request) {
        if (request == null) {
            return null;
        }

        mx.com.qtx.cotizador.dominio.promos.Promocion promocion = construirDominioDesdeDetalles(
            request.getDetalles(), request.getNombre(), request.getDescripcion(), null);
        return promocion;
    }

    private static mx.com.qtx.cotizador.dominio.promos.Promocion construirDominioDesdeDetalles(
        List<DetallePromocionRequest> detalles,
        String nombre,
        String descripcion,
        ParametrosNxMRequest parametrosNxMFromCreate
    ) {
        var builder = mx.com.qtx.cotizador.dominio.promos.Promocion.getBuilder();

        // 1) Base
        DetallePromocionRequest base = detalles != null
            ? detalles.stream().filter(d -> Boolean.TRUE.equals(d.getEsBase())).findFirst().orElse(null)
            : null;

        if (base != null && base.getTipoBase() == TipoPromocionBase.NXM && base.getParametrosNxM() != null) {
            builder.conPromocionBaseNXM(base.getParametrosNxM().getLlevent(), base.getParametrosNxM().getPaguen());
        } else {
            builder.conPromocionBaseSinDscto();
        }

        // 2) Acumulables
        if (detalles != null) {
            detalles.stream()
                .filter(d -> Boolean.FALSE.equals(d.getEsBase()))
                .forEach(d -> {
                    if (d.getTipoAcumulable() == TipoPromocionAcumulable.DESCUENTO_PLANO) {
                        double valor = d.getPorcentajeDescuentoPlano() != null ? d.getPorcentajeDescuentoPlano() : 0.0;
                        builder.agregarDsctoPlano((float) valor);
                    } else if (d.getTipoAcumulable() == TipoPromocionAcumulable.DESCUENTO_POR_CANTIDAD) {
                        Map<Integer, Double> mapa = new HashMap<>();
                        if (d.getEscalasDescuento() != null) {
                            for (EscalaDescuentoRequest esc : d.getEscalasDescuento()) {
                                Integer cantidad = esc.getCantidadMinimaEfectiva();
                                if (cantidad != null && esc.getDescuento() != null) {
                                    mapa.put(cantidad, esc.getDescuento());
                                }
                            }
                        }
                        builder.agregarDsctoXcantidad(mapa);
                    }
                });
        }

        mx.com.qtx.cotizador.dominio.promos.Promocion promocion = builder.build();
        promocion.setNombre(nombre);
        promocion.setDescripcion(descripcion);
        return promocion;
    }

    /**
     * Overload de conveniencia sin nombres de detalles.
     */
    public static PromocionResponse toResponse(mx.com.qtx.cotizador.dominio.promos.Promocion dominio,
                                               Integer idPromocion,
                                               LocalDate vigenciaDesde,
                                               LocalDate vigenciaHasta) {
        return toResponse(dominio, idPromocion, vigenciaDesde, vigenciaHasta, null);
    }

    /**
     * Convierte un objeto de dominio y metadatos a DTO de respuesta.
     * Permite pasar los nombres de los detalles en orden (base primero, luego acumulables).
     */
    public static PromocionResponse toResponse(mx.com.qtx.cotizador.dominio.promos.Promocion dominio,
                                               Integer idPromocion,
                                               LocalDate vigenciaDesde,
                                               LocalDate vigenciaHasta,
                                               List<String> nombresDetalles) {
        if (dominio == null) {
            return null;
        }

        List<DetallePromocionResponse> detalles = new ArrayList<>();

        // Base: descender hasta la base real
        mx.com.qtx.cotizador.dominio.promos.Promocion cursor = dominio;
        while (cursor instanceof PromAcumulable acum) {
            cursor = acum.getPromoBase();
        }

        DetallePromocionResponse base = new DetallePromocionResponse();
        base.setEsBase(true);
        if (cursor instanceof PromNXM nxm) {
            base.setTipoBase(TipoPromocionBase.NXM);
            base.setLlevent(nxm.getLleveN());
            base.setPaguen(nxm.getPagueM());
            base.setParametrosNxM(new ParametrosNxMResponse(nxm.getLleveN(), nxm.getPagueM()));
        } else {
            base.setTipoBase(TipoPromocionBase.SIN_DESCUENTO);
        }
        base.calcularDescripcionTipo();
        detalles.add(base);

        // Acumulables: recorrer desde el tope y acumular, luego invertir
        List<DetallePromocionResponse> acumList = new ArrayList<>();
        cursor = dominio;
        while (cursor instanceof PromAcumulable acum) {
            DetallePromocionResponse det = new DetallePromocionResponse();
            det.setEsBase(false);
            if (cursor instanceof mx.com.qtx.cotizador.dominio.promos.PromDsctoPlano plano) {
                det.setTipoAcumulable(TipoPromocionAcumulable.DESCUENTO_PLANO);
                det.setPorcentajeDescuentoPlano((double) plano.getPorcDescto());
            } else if (cursor instanceof mx.com.qtx.cotizador.dominio.promos.PromDsctoXcantidad xcant) {
                det.setTipoAcumulable(TipoPromocionAcumulable.DESCUENTO_POR_CANTIDAD);
                if (xcant.getMapCantidadVsDscto() != null) {
                    List<EscalaDescuentoResponse> escalas = xcant.getMapCantidadVsDscto().entrySet().stream()
                        .sorted(Map.Entry.comparingByKey())
                        .map(e -> new EscalaDescuentoResponse(e.getKey(), e.getValue()))
                        .collect(Collectors.toList());
                    det.setEscalasDescuento(escalas);
                }
            }
            det.calcularDescripcionTipo();
            acumList.add(det);
            cursor = acum.getPromoBase();
        }

        // Asignar nombres y armar lista final en orden base -> acumulables
        int nombreIdx = 0;
        // Base name
        if (nombresDetalles != null && nombresDetalles.size() > nombreIdx) {
            base.setNombre(nombresDetalles.get(nombreIdx));
        } else {
            base.setNombre("Base");
        }
        nombreIdx++;

        // Invertir para orden correcto y asignar nombres
        for (int i = acumList.size() - 1; i >= 0; i--) {
            DetallePromocionResponse det = acumList.get(i);
            if (nombresDetalles != null && nombresDetalles.size() > nombreIdx) {
                det.setNombre(nombresDetalles.get(nombreIdx));
            }
            detalles.add(det);
            nombreIdx++;
        }

        return PromocionResponse.builder()
            .idPromocion(idPromocion)
            .nombre(dominio.getNombre())
            .descripcion(dominio.getDescripcion())
            .vigenciaDesde(vigenciaDesde)
            .vigenciaHasta(vigenciaHasta)
            .detalles(detalles)
            .build();
    }
} 