package mx.com.qtx.cotizador.servicio.wrapper;

import java.util.Map;
import java.util.stream.Collectors;

import mx.com.qtx.cotizador.entidad.DetallePromocion;
import mx.com.qtx.cotizador.dominio.promos.Promocion;
import mx.com.qtx.cotizador.dominio.promos.PromocionBuilder;

public class PromocionEntityConverter {

    /**
     * Convierte una entidad de promoción de la base de datos 
     * al objeto de dominio correspondiente
     */
    public static Promocion convertToPromocion(mx.com.qtx.cotizador.entidad.Promocion entidad) {
        if (entidad == null || entidad.getDetalles().isEmpty()) {
            return null;
        }

        PromocionBuilder builder = Promocion.getBuilder();
        
        // Ordenar detalles: primero el base, luego los acumulables
        var detallesOrdenados = entidad.getDetalles().stream()
            .sorted((d1, d2) -> {
                if (d1.getEsBase() && !d2.getEsBase()) return -1;
                if (!d1.getEsBase() && d2.getEsBase()) return 1;
                return d1.getIdDetallePromocion().compareTo(d2.getIdDetallePromocion());
            })
            .toList();

        // 1. Configurar promoción base
        DetallePromocion detalleBase = detallesOrdenados.stream()
            .filter(DetallePromocion::getEsBase)
            .findFirst()
            .orElse(null);

        if (detalleBase != null) {
            configurarPromocionBase(builder, detalleBase);
        } else {
            builder.conPromocionBaseSinDscto();
        }

        // 2. Agregar promociones acumulables
        detallesOrdenados.stream()
            .filter(detalle -> !detalle.getEsBase())
            .forEach(detalle -> configurarPromocionAcumulable(builder, detalle));

        Promocion promocion = builder.build();
        promocion.setNombre(entidad.getNombre());
        promocion.setDescripcion(entidad.getDescripcion());
        
        return promocion;
    }

    private static void configurarPromocionBase(PromocionBuilder builder, DetallePromocion detalle) {
        String tipoBase = detalle.getTipoPromBase();
        
        if ("NXM".equals(tipoBase)) {
            builder.conPromocionBaseNXM(detalle.getLlevent(), detalle.getPaguen());
        } else {
            builder.conPromocionBaseSinDscto();
        }
    }

    private static void configurarPromocionAcumulable(PromocionBuilder builder, DetallePromocion detalle) {
        String tipoAcumulable = detalle.getTipoPromAcumulable();
        
        switch (tipoAcumulable) {
            case "DESCUENTO_PLANO":
                builder.agregarDsctoPlano(detalle.getPorcDctoPlano().floatValue());
                break;
                
            case "DESCUENTO_POR_CANTIDAD":
                Map<Integer, Double> mapCantVsDscto = detalle.getDescuentosPorCantidad().stream()
                    .collect(Collectors.toMap(
                        d -> d.getCantidad(),
                        d -> d.getDscto()
                    ));
                builder.agregarDsctoXcantidad(mapCantVsDscto);
                break;
                
            default:
                // Log warning sobre tipo desconocido
                break;
        }
    }

    /**
     * Convierte un objeto de dominio Promocion a entidad de persistencia
     * (Para operaciones de guardado)
     */
    public static mx.com.qtx.cotizador.entidad.Promocion convertToEntity(Promocion promocion) {
        // TODO: Implementar si necesitas guardar promociones desde el dominio
        throw new UnsupportedOperationException("Conversión de dominio a entidad no implementada aún");
    }
} 