package mx.com.qtx.cotizador.servicio.wrapper;

import java.util.Map;
import java.util.stream.Collectors;

import mx.com.qtx.cotizador.entidad.DetallePromocion;
import mx.com.qtx.cotizador.dominio.promos.Promocion;
import mx.com.qtx.cotizador.dominio.promos.PromocionBuilder;
import mx.com.qtx.cotizador.dominio.promos.PromAcumulable;
import mx.com.qtx.cotizador.dominio.promos.PromBase;
import mx.com.qtx.cotizador.dominio.promos.PromDsctoPlano;
import mx.com.qtx.cotizador.dominio.promos.PromDsctoXcantidad;
import mx.com.qtx.cotizador.dominio.promos.PromNXM;

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
        if (promocion == null) {
            return null;
        }

        mx.com.qtx.cotizador.entidad.Promocion entidad = new mx.com.qtx.cotizador.entidad.Promocion();
        entidad.setNombre(promocion.getNombre());
        entidad.setDescripcion(promocion.getDescripcion());
        // Vigencias deben establecerse fuera con datos del request (no existen en dominio)

        // Reconstruir lista de detalles desde el objeto de dominio
        // Orden: primero base, luego acumulables
        DetallePromocion detalleBase = construirDetalleBaseDesdeDominio(promocion, entidad);
        entidad.addDetalle(detalleBase);

        agregarDetallesAcumulablesDesdeDominio(promocion, entidad, detalleBase);

        return entidad;
    }

    /**
     * Actualiza una entidad existente a partir de un objeto de dominio, limpiando y
     * reconstruyendo los detalles para reflejar la nueva configuración.
     */
    public static mx.com.qtx.cotizador.entidad.Promocion mergeIntoEntity(
        Promocion promocionDominio,
        mx.com.qtx.cotizador.entidad.Promocion entidadExistente
    ) {
        if (promocionDominio == null || entidadExistente == null) {
            return entidadExistente;
        }

        entidadExistente.setNombre(promocionDominio.getNombre());
        entidadExistente.setDescripcion(promocionDominio.getDescripcion());

        // Limpiar detalles actuales y reconstruir
        entidadExistente.getDetalles().clear();

        DetallePromocion detalleBase = construirDetalleBaseDesdeDominio(promocionDominio, entidadExistente);
        entidadExistente.addDetalle(detalleBase);

        agregarDetallesAcumulablesDesdeDominio(promocionDominio, entidadExistente, detalleBase);

        return entidadExistente;
    }

    private static DetallePromocion construirDetalleBaseDesdeDominio(
        Promocion promocionDominio,
        mx.com.qtx.cotizador.entidad.Promocion entidad
    ) {
        DetallePromocion detalle = new DetallePromocion();
        detalle.setPromocion(entidad);
        detalle.setEsBase(true);
        detalle.setNombre("Base");

        if (promocionDominio instanceof PromNXM promNXM) {
            detalle.setTipoPromBase("NXM");
            detalle.setLlevent(promNXM.getLleveN());
            detalle.setPaguen(promNXM.getPagueM());
            detalle.setPorcDctoPlano(0.0);
        } else if (promocionDominio instanceof PromBase) {
            // Base sin descuento
            detalle.setTipoPromBase("SIN_DESCUENTO");
            detalle.setLlevent(0);
            detalle.setPaguen(0);
            detalle.setPorcDctoPlano(0.0);
        } else if (promocionDominio instanceof PromAcumulable promAcum) {
            // Si el tope visible es acumulable, bajar hasta su base
            Promocion base = promAcum.getPromoBase();
            return construirDetalleBaseDesdeDominio(base, entidad);
        } else {
            // Fallback: sin descuento
            detalle.setTipoPromBase("SIN_DESCUENTO");
            detalle.setLlevent(0);
            detalle.setPaguen(0);
            detalle.setPorcDctoPlano(0.0);
        }

        return detalle;
    }

    private static void agregarDetallesAcumulablesDesdeDominio(
        Promocion promocionDominio,
        mx.com.qtx.cotizador.entidad.Promocion entidad,
        DetallePromocion detalleBase
    ) {
        // Recorremos la cadena de decoradores acumulables en orden
        Promocion cursor = promocionDominio;
        while (cursor instanceof PromAcumulable acum) {
            DetallePromocion detalleAcum = new DetallePromocion();
            detalleAcum.setPromocion(entidad);
            detalleAcum.setEsBase(false);
            detalleAcum.setNombre("Acumulable");
            detalleAcum.setTipoPromBase(null);
            detalleAcum.setLlevent(0);
            detalleAcum.setPaguen(0);
            detalleAcum.setPorcDctoPlano(0.0);

            if (acum instanceof PromDsctoPlano plano) {
                detalleAcum.setTipoPromAcumulable("DESCUENTO_PLANO");
                detalleAcum.setPorcDctoPlano((double) plano.getPorcDescto());
            } else if (acum instanceof PromDsctoXcantidad xCant) {
                detalleAcum.setTipoPromAcumulable("DESCUENTO_POR_CANTIDAD");
                // Crear escalas
                int num = 1;
                for (var entry : xCant.getMapCantidadVsDscto().entrySet()) {
                    var escala = new mx.com.qtx.cotizador.entidad.DetallePromDsctoXCant();
                    escala.setNumDscto(num++);
                    escala.setCantidad(entry.getKey());
                    escala.setDscto(entry.getValue());
                    // Relacionar
                    detalleAcum.addDescuentoPorCantidad(escala);
                }
            }

            entidad.addDetalle(detalleAcum);
            cursor = acum.getPromoBase();
        }
    }
} 