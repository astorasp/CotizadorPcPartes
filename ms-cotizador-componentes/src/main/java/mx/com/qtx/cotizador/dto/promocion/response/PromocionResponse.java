package mx.com.qtx.cotizador.dto.promocion.response;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO de respuesta completo para promociones (Caso 6.3: Consultar promociones)
 * 
 * Contiene toda la información de la promoción incluyendo detalles jerárquicos
 * y metadatos calculados para facilitar la visualización en el frontend.
 */
public class PromocionResponse {
    
    private Integer idPromocion;
    private String nombre;
    private String descripcion;
    private LocalDate vigenciaDesde;
    private LocalDate vigenciaHasta;
    
    // Lista de detalles con configuración completa
    private List<DetallePromocionResponse> detalles;
    
    // Metadatos calculados
    private String estadoVigencia; // VIGENTE, EXPIRADA, FUTURA
    private Integer diasRestantes; // Días hasta vencimiento
    private Integer totalDetalles;
    private String tipoPromocionPrincipal; // Descripción del tipo base
    private Boolean tieneDescuentosAcumulables;
    
    // Constructores
    public PromocionResponse() {
        // Constructor vacío para Jackson
    }
    
    // Builder pattern para facilitar construcción
    public static Builder builder() {
        return new Builder();
    }
    
    // Getters y setters
    public Integer getIdPromocion() {
        return idPromocion;
    }
    
    public void setIdPromocion(Integer idPromocion) {
        this.idPromocion = idPromocion;
    }
    
    public String getNombre() {
        return nombre;
    }
    
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    
    public String getDescripcion() {
        return descripcion;
    }
    
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
    
    public LocalDate getVigenciaDesde() {
        return vigenciaDesde;
    }
    
    public void setVigenciaDesde(LocalDate vigenciaDesde) {
        this.vigenciaDesde = vigenciaDesde;
    }
    
    public LocalDate getVigenciaHasta() {
        return vigenciaHasta;
    }
    
    public void setVigenciaHasta(LocalDate vigenciaHasta) {
        this.vigenciaHasta = vigenciaHasta;
    }
    
    public List<DetallePromocionResponse> getDetalles() {
        return detalles;
    }
    
    public void setDetalles(List<DetallePromocionResponse> detalles) {
        this.detalles = detalles;
        calcularMetadatos();
    }
    
    public String getEstadoVigencia() {
        return estadoVigencia;
    }
    
    public void setEstadoVigencia(String estadoVigencia) {
        this.estadoVigencia = estadoVigencia;
    }
    
    public Integer getDiasRestantes() {
        return diasRestantes;
    }
    
    public void setDiasRestantes(Integer diasRestantes) {
        this.diasRestantes = diasRestantes;
    }
    
    public Integer getTotalDetalles() {
        return totalDetalles;
    }
    
    public void setTotalDetalles(Integer totalDetalles) {
        this.totalDetalles = totalDetalles;
    }
    
    public String getTipoPromocionPrincipal() {
        return tipoPromocionPrincipal;
    }
    
    public void setTipoPromocionPrincipal(String tipoPromocionPrincipal) {
        this.tipoPromocionPrincipal = tipoPromocionPrincipal;
    }
    
    public Boolean getTieneDescuentosAcumulables() {
        return tieneDescuentosAcumulables;
    }
    
    public void setTieneDescuentosAcumulables(Boolean tieneDescuentosAcumulables) {
        this.tieneDescuentosAcumulables = tieneDescuentosAcumulables;
    }
    
    /**
     * Calcula metadatos basados en los datos actuales
     */
    public void calcularMetadatos() {
        calcularEstadoVigencia();
        calcularTotalDetalles();
        calcularTipoPromocionPrincipal();
        calcularDescuentosAcumulables();
    }
    
    private void calcularEstadoVigencia() {
        LocalDate hoy = LocalDate.now();
        
        if (vigenciaDesde == null || vigenciaHasta == null) {
            estadoVigencia = "INDEFINIDA";
            diasRestantes = null;
            return;
        }
        
        if (hoy.isBefore(vigenciaDesde)) {
            estadoVigencia = "FUTURA";
            diasRestantes = (int) java.time.temporal.ChronoUnit.DAYS.between(hoy, vigenciaDesde);
        } else if (hoy.isAfter(vigenciaHasta)) {
            estadoVigencia = "EXPIRADA";
            diasRestantes = 0;
        } else {
            estadoVigencia = "VIGENTE";
            diasRestantes = (int) java.time.temporal.ChronoUnit.DAYS.between(hoy, vigenciaHasta);
        }
    }
    
    private void calcularTotalDetalles() {
        totalDetalles = (detalles != null) ? detalles.size() : 0;
    }
    
    private void calcularTipoPromocionPrincipal() {
        if (detalles == null || detalles.isEmpty()) {
            tipoPromocionPrincipal = "Promoción sin descuento";
            return;
        }
        
        // Buscar el detalle base
        DetallePromocionResponse detalleBase = detalles.stream()
            .filter(d -> d.getEsBase() != null && d.getEsBase())
            .findFirst()
            .orElse(null);
        
        if (detalleBase != null) {
            tipoPromocionPrincipal = detalleBase.getDescripcionTipo();
        } else {
            tipoPromocionPrincipal = "Sin promoción base configurada";
        }
    }
    
    private void calcularDescuentosAcumulables() {
        if (detalles == null) {
            tieneDescuentosAcumulables = false;
            return;
        }
        
        tieneDescuentosAcumulables = detalles.stream()
            .anyMatch(d -> d.getEsBase() != null && !d.getEsBase());
    }
    
    @Override
    public String toString() {
        return "PromocionResponse{" +
                "idPromocion=" + idPromocion +
                ", nombre='" + nombre + '\'' +
                ", estadoVigencia='" + estadoVigencia + '\'' +
                ", totalDetalles=" + totalDetalles +
                ", tipoPromocionPrincipal='" + tipoPromocionPrincipal + '\'' +
                '}';
    }
    
    // Builder class
    public static class Builder {
        private PromocionResponse promocion = new PromocionResponse();
        
        public Builder idPromocion(Integer idPromocion) {
            promocion.setIdPromocion(idPromocion);
            return this;
        }
        
        public Builder nombre(String nombre) {
            promocion.setNombre(nombre);
            return this;
        }
        
        public Builder descripcion(String descripcion) {
            promocion.setDescripcion(descripcion);
            return this;
        }
        
        public Builder vigenciaDesde(LocalDate vigenciaDesde) {
            promocion.setVigenciaDesde(vigenciaDesde);
            return this;
        }
        
        public Builder vigenciaHasta(LocalDate vigenciaHasta) {
            promocion.setVigenciaHasta(vigenciaHasta);
            return this;
        }
        
        public Builder detalles(List<DetallePromocionResponse> detalles) {
            promocion.setDetalles(detalles);
            return this;
        }
        
        public PromocionResponse build() {
            promocion.calcularMetadatos();
            return promocion;
        }
    }
}