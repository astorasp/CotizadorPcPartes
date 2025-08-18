package mx.com.qtx.cotizador.dominio.core;

import java.util.HashMap;
import java.util.Map;

import mx.com.qtx.cotizador.dominio.pedidos.IPresupuesto;
import mx.com.qtx.cotizador.dominio.pedidos.excepciones.PresupuestoNoCargadoExcepcion;

public class CotizacionPresupuestoAdapter implements IPresupuesto {

    private Cotizacion cotizacionAdaptee; // El objeto que adaptamos (la cotización)

    public CotizacionPresupuestoAdapter(Cotizacion cotizacionAdaptee) throws PresupuestoNoCargadoExcepcion {
        if(cotizacionAdaptee == null){
            throw new PresupuestoNoCargadoExcepcion();
        }        
        this.cotizacionAdaptee = cotizacionAdaptee;
    }

    @Override
    public String getDescripcionArticulo(String idArticulo) {
        DetalleCotizacion detalle = this.getDetallePorId(idArticulo);
        return (detalle != null && detalle.getComponente() != null) ? 
            detalle.getComponente().getDescripcion() : "Descripción no encontrada";
    }

    @Override
    public Map<String, Integer> getCantidadesXIdArticulo() {
        Map<String, Integer> cantidades = new HashMap<>();
        for (DetalleCotizacion detalle : cotizacionAdaptee.getDetalles()) {
            if (detalle.getComponente() != null) {
                cantidades.put(detalle.getComponente().getId().toString(), detalle.getCantidad());
            }
        }
        return cantidades;
    }

    @Override
    public Map<String, Object> getDatosArticulo(String idArticulo) {
        DetalleCotizacion detalle = this.getDetallePorId(idArticulo);
        if (detalle == null) {
            return new HashMap<>(); // Vacío si no se encuentra
        }
        // Devolvemos un mapa con los datos que podrían ser útiles del detalle
        // Esto es flexible según lo que realmente necesite IPresupuesto
        Map<String, Object> datos = new HashMap<>();
        datos.put("descripcion", detalle.getComponente() != null ? detalle.getComponente().getDescripcion() : "");
        datos.put("cantidad", detalle.getCantidad());
        datos.put("precioBase", detalle.getPrecioUnitario());
        datos.put("importeTotalLinea", detalle.getSubtotal());
        // Se podrían añadir más datos si fueran necesarios
        return datos;
    }

    //Metodo para buscar un detalle de cotizacion por el id de componente
    private DetalleCotizacion getDetallePorId(String idArticulo) {
        return cotizacionAdaptee
            .getDetalles()
            .stream()
            .filter(x -> x.getComponente() != null && x.getComponente().getId().toString().equals(idArticulo))
            .findFirst()
            .orElse(null);  
    }
} 