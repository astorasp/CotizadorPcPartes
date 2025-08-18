package mx.com.qtx.cotizador.dto.promocion.request;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO para la creación completa de promociones (Caso 6.1: Agregar promoción)
 * 
 * Maneja la complejidad completa del sistema de promociones:
 * - Datos básicos (nombre, descripción, vigencia)
 * - Lista de detalles con diferentes tipos de promoción
 * - Validaciones cruzadas entre campos
 */
public class PromocionCreateRequest {
    
    @NotBlank(message = "El nombre de la promoción es requerido")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    private String nombre;
    
    @NotBlank(message = "La descripción de la promoción es requerida")
    @Size(min = 5, max = 500, message = "La descripción debe tener entre 5 y 500 caracteres")
    private String descripcion;
    
    @NotNull(message = "La fecha de inicio de vigencia es requerida")
    private LocalDate vigenciaDesde;
    
    @NotNull(message = "La fecha de fin de vigencia es requerida")
    private LocalDate vigenciaHasta;
    
    @NotEmpty(message = "La promoción debe tener al menos un detalle")
    @Valid
    private List<DetallePromocionRequest> detalles;
    
    // Constructores
    public PromocionCreateRequest() {
        // Constructor vacío para Jackson
    }
    
    public PromocionCreateRequest(String nombre, String descripcion, 
                                 LocalDate vigenciaDesde, LocalDate vigenciaHasta,
                                 List<DetallePromocionRequest> detalles) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.vigenciaDesde = vigenciaDesde;
        this.vigenciaHasta = vigenciaHasta;
        this.detalles = detalles;
    }
    
    // Getters y setters
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
    
    public List<DetallePromocionRequest> getDetalles() {
        return detalles;
    }
    
    public void setDetalles(List<DetallePromocionRequest> detalles) {
        this.detalles = detalles;
    }
    
    /**
     * Valida reglas de negocio complejas de la promoción
     */
    public boolean esValida() {
        // Validación básica
        if (!validarCamposBasicos()) {
            return false;
        }
        
        // Validación de fechas
        if (!validarFechas()) {
            return false;
        }
        
        // Validación de detalles
        if (!validarDetalles()) {
            return false;
        }
        
        return true;
    }
    
    private boolean validarCamposBasicos() {
        return nombre != null && !nombre.trim().isEmpty() &&
               descripcion != null && !descripcion.trim().isEmpty() &&
               vigenciaDesde != null && vigenciaHasta != null &&
               detalles != null && !detalles.isEmpty();
    }
    
    private boolean validarFechas() {
        // La fecha de inicio debe ser anterior o igual a la fecha de fin
        return !vigenciaDesde.isAfter(vigenciaHasta);
    }
    
    private boolean validarDetalles() {
        // Debe tener exactamente un detalle base
        long detallesBase = detalles.stream()
            .filter(detalle -> detalle.getEsBase() != null && detalle.getEsBase())
            .count();
        
        if (detallesBase != 1) {
            return false; // Debe tener exactamente un detalle base
        }
        
        // Todos los detalles deben ser válidos
        return detalles.stream().allMatch(DetallePromocionRequest::esValido);
    }
    
    /**
     * Retorna el detalle base de la promoción
     */
    public DetallePromocionRequest getDetalleBase() {
        return detalles.stream()
            .filter(detalle -> detalle.getEsBase() != null && detalle.getEsBase())
            .findFirst()
            .orElse(null);
    }
    
    /**
     * Retorna los detalles acumulables de la promoción
     */
    public List<DetallePromocionRequest> getDetallesAcumulables() {
        return detalles.stream()
            .filter(detalle -> detalle.getEsBase() != null && !detalle.getEsBase())
            .toList();
    }
    
    @Override
    public String toString() {
        return "PromocionCreateRequest{" +
                "nombre='" + nombre + '\'' +
                ", descripcion='" + descripcion + '\'' +
                ", vigenciaDesde=" + vigenciaDesde +
                ", vigenciaHasta=" + vigenciaHasta +
                ", totalDetalles=" + (detalles != null ? detalles.size() : 0) +
                '}';
    }
} 