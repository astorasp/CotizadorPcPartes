package mx.com.qtx.cotizador.servicio.promocion;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Arrays;

import mx.com.qtx.cotizador.dto.common.response.ApiResponse;
import mx.com.qtx.cotizador.dto.promocion.request.PromocionCreateRequest;
import mx.com.qtx.cotizador.dto.promocion.request.PromocionUpdateRequest;
import mx.com.qtx.cotizador.dto.promocion.response.PromocionResponse;
import mx.com.qtx.cotizador.entidad.Promocion;
import mx.com.qtx.cotizador.repositorio.PromocionRepositorio;

/**
 * Tests unitarios para PromocionServicio.
 * Verifica la lógica de negocio de la capa de servicios para promociones.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PromocionServicio - Tests de Capa de Servicio")
class PromocionServicioTest {
    
    @Mock
    private PromocionRepositorio promocionRepositorio;
    
    @InjectMocks
    private PromocionServicio promocionServicio;
    
    // ===== TESTS DE CREACIÓN =====
    
    /**
     * Dado: Una solicitud válida de creación de promoción
     * Cuando: Se invoca crearPromocion
     * Entonces: Se crea la promoción correctamente
     */
    @Test
    @DisplayName("Crear promoción con datos válidos debe retornar éxito")
    void crearPromocion_ConDatosValidos_DebeRetornarExito() {
        // Given
        PromocionCreateRequest request = crearPromocionRequestValida();
        Promocion promocionGuardada = crearPromocionEntity(1, "Promoción Test");

        // Debug - verificar que el request es válido
        assertThat(request.esValida()).isTrue();

        when(promocionRepositorio.findByNombre(anyString())).thenReturn(null);
        when(promocionRepositorio.save(any(Promocion.class))).thenReturn(promocionGuardada);

        // When
        ApiResponse<PromocionResponse> resultado = promocionServicio.crearPromocion(request);

        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado.getCodigo()).describedAs("Código de error: %s, Mensaje: %s",
                                                      resultado.getCodigo(), resultado.getMensaje()).isEqualTo("0");
        assertThat(resultado.getDatos()).describedAs("Mensaje de error: %s", resultado.getMensaje()).isNotNull();
        verify(promocionRepositorio, times(1)).save(any(Promocion.class));
    }
    
    /**
     * Dado: Una solicitud nula
     * Cuando: Se invoca crearPromocion
     * Entonces: Se retorna error de validación
     */
    @Test
    @DisplayName("Crear promoción con request nulo debe retornar error de validación")
    void crearPromocion_ConRequestNulo_DebeRetornarErrorValidacion() {
        // When
        ApiResponse<PromocionResponse> resultado = promocionServicio.crearPromocion(null);
        
        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado.getCodigo()).isNotEqualTo("0");
        assertThat(resultado.getMensaje()).contains("requeridos");
        verify(promocionRepositorio, never()).save(any());
    }
    
    /**
     * Dado: Una promoción que ya existe con el mismo nombre
     * Cuando: Se intenta crear otra con mismo nombre
     * Entonces: Se retorna error de duplicación
     */
    @Test
    @DisplayName("Crear promoción con nombre duplicado debe retornar error")
    void crearPromocion_ConNombreDuplicado_DebeRetornarError() {
        // Given
        PromocionCreateRequest request = crearPromocionRequestValida();
        Promocion promocionExistente = crearPromocionEntity(1, request.getNombre());
        
        when(promocionRepositorio.findByNombre(request.getNombre())).thenReturn(promocionExistente);
        
        // When
        ApiResponse<PromocionResponse> resultado = promocionServicio.crearPromocion(request);
        
        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado.getCodigo()).isNotEqualTo("0");
        assertThat(resultado.getMensaje()).contains("Ya existe");
        verify(promocionRepositorio, never()).save(any());
    }
    
    // ===== TESTS DE BÚSQUEDA =====
    
    /**
     * Dado: Un ID válido de promoción existente
     * Cuando: Se busca por ID
     * Entonces: Se retorna la promoción encontrada
     */
    @Test
    @DisplayName("Buscar promoción por ID válido debe retornar promoción")
    void buscarPorId_ConIdValido_DebeRetornarPromocion() {
        // Given
        Integer id = 1;
        Promocion promocion = crearPromocionEntity(id, "Promoción Test");
        
        when(promocionRepositorio.findById(id)).thenReturn(Optional.of(promocion));
        
        // When
        ApiResponse<PromocionResponse> resultado = promocionServicio.buscarPorId(id);
        
        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado.getCodigo()).isEqualTo("0");
        assertThat(resultado.getDatos()).isNotNull();
    }
    
    /**
     * Dado: Un ID nulo
     * Cuando: Se busca por ID
     * Entonces: Se retorna error de campo requerido
     */
    @Test
    @DisplayName("Buscar promoción con ID nulo debe retornar error")
    void buscarPorId_ConIdNulo_DebeRetornarError() {
        // When
        ApiResponse<PromocionResponse> resultado = promocionServicio.buscarPorId(null);
        
        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado.getCodigo()).isNotEqualTo("0");
        assertThat(resultado.getMensaje()).contains("requerido");
        verify(promocionRepositorio, never()).findById(any());
    }
    
    /**
     * Dado: Un ID de promoción inexistente
     * Cuando: Se busca por ID
     * Entonces: Se retorna error de no encontrado
     */
    @Test
    @DisplayName("Buscar promoción inexistente debe retornar error")
    void buscarPorId_PromocionInexistente_DebeRetornarError() {
        // Given
        Integer id = 999;
        when(promocionRepositorio.findById(id)).thenReturn(Optional.empty());
        
        // When
        ApiResponse<PromocionResponse> resultado = promocionServicio.buscarPorId(id);
        
        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado.getCodigo()).isNotEqualTo("0");
        assertThat(resultado.getMensaje()).contains("no encontrada");
    }
    
    // ===== TESTS DE OBTENER TODAS =====
    
    /**
     * Dado: Promociones existentes en el repositorio
     * Cuando: Se obtienen todas las promociones
     * Entonces: Se retorna lista de promociones
     */
    @Test
    @DisplayName("Obtener todas las promociones debe retornar lista")
    void obtenerTodasLasPromociones_ConPromocionesExistentes_DebeRetornarLista() {
        // Given
        List<Promocion> promociones = Arrays.asList(
            crearPromocionEntity(1, "Promoción 1"),
            crearPromocionEntity(2, "Promoción 2")
        );
        
        when(promocionRepositorio.findAll()).thenReturn(promociones);
        
        // When
        ApiResponse<List<PromocionResponse>> resultado = promocionServicio.obtenerTodasLasPromociones();
        
        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado.getCodigo()).isEqualTo("0");
        assertThat(resultado.getDatos()).isNotNull();
        assertThat(resultado.getDatos()).hasSize(2);
    }
    
    /**
     * Dado: Un repositorio vacío
     * Cuando: Se obtienen todas las promociones
     * Entonces: Se retorna lista vacía exitosamente
     */
    @Test
    @DisplayName("Obtener todas las promociones con repositorio vacío debe retornar lista vacía")
    void obtenerTodasLasPromociones_RepositorioVacio_DebeRetornarListaVacia() {
        // Given
        when(promocionRepositorio.findAll()).thenReturn(Arrays.asList());
        
        // When
        ApiResponse<List<PromocionResponse>> resultado = promocionServicio.obtenerTodasLasPromociones();
        
        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado.getCodigo()).isEqualTo("0");
        assertThat(resultado.getDatos()).isEmpty();
    }
    
    // ===== TESTS DE ACTUALIZACIÓN =====
    
    /**
     * Dado: Una solicitud válida de actualización
     * Cuando: Se actualiza la promoción
     * Entonces: Se actualiza correctamente
     */
    @Test
    @DisplayName("Actualizar promoción con datos válidos debe retornar éxito")
    void actualizarPromocion_ConDatosValidos_DebeRetornarExito() {
        // Given
        Integer id = 1;
        PromocionUpdateRequest request = crearPromocionUpdateRequestValida();
        Promocion promocionExistente = crearPromocionEntity(id, "Promoción Original");
        Promocion promocionActualizada = crearPromocionEntity(id, request.getNombre());
        
        when(promocionRepositorio.findById(id)).thenReturn(Optional.of(promocionExistente));
        when(promocionRepositorio.findByNombre(request.getNombre())).thenReturn(null);
        when(promocionRepositorio.save(any(Promocion.class))).thenReturn(promocionActualizada);
        
        // When
        ApiResponse<PromocionResponse> resultado = promocionServicio.actualizarPromocion(id, request);
        
        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado.getCodigo()).isEqualTo("0");
        assertThat(resultado.getDatos()).isNotNull();
        verify(promocionRepositorio, times(1)).save(any(Promocion.class));
    }
    
    /**
     * Dado: Un ID nulo para actualización
     * Cuando: Se intenta actualizar
     * Entonces: Se retorna error de campo requerido
     */
    @Test
    @DisplayName("Actualizar promoción con ID nulo debe retornar error")
    void actualizarPromocion_ConIdNulo_DebeRetornarError() {
        // Given
        PromocionUpdateRequest request = crearPromocionUpdateRequestValida();
        
        // When
        ApiResponse<PromocionResponse> resultado = promocionServicio.actualizarPromocion(null, request);
        
        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado.getCodigo()).isNotEqualTo("0");
        assertThat(resultado.getMensaje()).contains("requerido");
        verify(promocionRepositorio, never()).save(any());
    }
    
    /**
     * Dado: Una promoción inexistente para actualizar
     * Cuando: Se intenta actualizar
     * Entonces: Se retorna error de no encontrado
     */
    @Test
    @DisplayName("Actualizar promoción inexistente debe retornar error")
    void actualizarPromocion_PromocionInexistente_DebeRetornarError() {
        // Given
        Integer id = 999;
        PromocionUpdateRequest request = crearPromocionUpdateRequestValida();
        
        when(promocionRepositorio.findById(id)).thenReturn(Optional.empty());
        
        // When
        ApiResponse<PromocionResponse> resultado = promocionServicio.actualizarPromocion(id, request);
        
        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado.getCodigo()).isNotEqualTo("0");
        assertThat(resultado.getMensaje()).contains("no encontrada");
        verify(promocionRepositorio, never()).save(any());
    }
    
    // ===== TESTS DE ELIMINACIÓN =====
    
    /**
     * Dado: Un ID válido de promoción existente
     * Cuando: Se elimina la promoción
     * Entonces: Se elimina correctamente
     */
    @Test
    @DisplayName("Eliminar promoción existente debe retornar éxito")
    void eliminarPromocion_PromocionExistente_DebeRetornarExito() {
        // Given
        Integer id = 1;
        Promocion promocion = crearPromocionEntity(id, "Promoción Test");
        
        when(promocionRepositorio.findById(id)).thenReturn(Optional.of(promocion));
        
        // When
        ApiResponse<Void> resultado = promocionServicio.eliminarPromocion(id);
        
        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado.getCodigo()).isEqualTo("0");
        verify(promocionRepositorio, times(1)).delete(promocion);
    }
    
    /**
     * Dado: Un ID nulo
     * Cuando: Se intenta eliminar
     * Entonces: Se retorna error de campo requerido
     */
    @Test
    @DisplayName("Eliminar promoción con ID nulo debe retornar error")
    void eliminarPromocion_ConIdNulo_DebeRetornarError() {
        // When
        ApiResponse<Void> resultado = promocionServicio.eliminarPromocion(null);
        
        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado.getCodigo()).isNotEqualTo("0");
        assertThat(resultado.getMensaje()).contains("requerido");
        verify(promocionRepositorio, never()).delete(any());
    }
    
    /**
     * Dado: Un ID de promoción inexistente
     * Cuando: Se intenta eliminar
     * Entonces: Se retorna error de no encontrado
     */
    @Test
    @DisplayName("Eliminar promoción inexistente debe retornar error")
    void eliminarPromocion_PromocionInexistente_DebeRetornarError() {
        // Given
        Integer id = 999;
        when(promocionRepositorio.findById(id)).thenReturn(Optional.empty());
        
        // When
        ApiResponse<Void> resultado = promocionServicio.eliminarPromocion(id);
        
        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado.getCodigo()).isNotEqualTo("0");
        assertThat(resultado.getMensaje()).contains("no encontrada");
        verify(promocionRepositorio, never()).delete(any());
    }
    
    // ===== TESTS DE CASOS EDGE =====
    
    /**
     * Dado: Una actualización con request nulo
     * Cuando: Se intenta actualizar
     * Entonces: Se retorna error de validación
     */
    @Test
    @DisplayName("Actualizar promoción con request nulo debe retornar error")
    void actualizarPromocion_ConRequestNulo_DebeRetornarError() {
        // When
        ApiResponse<PromocionResponse> resultado = promocionServicio.actualizarPromocion(1, null);
        
        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado.getCodigo()).isNotEqualTo("0");
        assertThat(resultado.getMensaje()).contains("requeridos");
        verify(promocionRepositorio, never()).save(any());
    }
    
    /**
     * Dado: Una actualización con nombre que ya existe en otra promoción
     * Cuando: Se intenta actualizar
     * Entonces: Se retorna error de duplicación
     */
    @Test
    @DisplayName("Actualizar promoción con nombre duplicado debe retornar error")
    void actualizarPromocion_ConNombreDuplicado_DebeRetornarError() {
        // Given
        Integer id = 1;
        PromocionUpdateRequest request = crearPromocionUpdateRequestValida();
        Promocion promocionExistente = crearPromocionEntity(id, "Promoción Original");
        Promocion otraPromocion = crearPromocionEntity(2, request.getNombre());
        
        when(promocionRepositorio.findById(id)).thenReturn(Optional.of(promocionExistente));
        when(promocionRepositorio.findByNombre(request.getNombre())).thenReturn(otraPromocion);
        
        // When
        ApiResponse<PromocionResponse> resultado = promocionServicio.actualizarPromocion(id, request);
        
        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado.getCodigo()).isNotEqualTo("0");
        assertThat(resultado.getMensaje()).contains("Ya existe otra");
        verify(promocionRepositorio, never()).save(any());
    }
    
    // ===== MÉTODOS AUXILIARES =====
    
    private PromocionCreateRequest crearPromocionRequestValida() {
        PromocionCreateRequest request = new PromocionCreateRequest();
        request.setNombre("Promoción Test");
        request.setDescripcion("Descripción de prueba");
        request.setVigenciaDesde(LocalDate.now());
        request.setVigenciaHasta(LocalDate.now().plusDays(30));
        // Crear una lista de detalles válidos para que esValida() retorne true
        request.setDetalles(java.util.Arrays.asList());
        return request;
    }
    
    private PromocionUpdateRequest crearPromocionUpdateRequestValida() {
        PromocionUpdateRequest request = new PromocionUpdateRequest();
        request.setNombre("Promoción Actualizada");
        request.setDescripcion("Descripción actualizada");
        request.setVigenciaDesde(LocalDate.now());
        request.setVigenciaHasta(LocalDate.now().plusDays(60));
        // Crear una lista de detalles válidos para que esValida() retorne true
        request.setDetalles(java.util.Arrays.asList());
        return request;
    }
    
    private Promocion crearPromocionEntity(Integer id, String nombre) {
        Promocion promocion = new Promocion();
        promocion.setIdPromocion(id);
        promocion.setNombre(nombre);
        promocion.setDescripcion("Descripción de " + nombre);
        promocion.setVigenciaDesde(LocalDate.now());
        promocion.setVigenciaHasta(LocalDate.now().plusDays(30));

        // Agregar un detalle base válido para que el converter no retorne null
        mx.com.qtx.cotizador.entidad.DetallePromocion detalleBase = new mx.com.qtx.cotizador.entidad.DetallePromocion();
        detalleBase.setIdDetallePromocion(1);
        detalleBase.setEsBase(true);
        detalleBase.setNombre("Base");
        detalleBase.setTipoPromBase("SIN_DESCUENTO");
        detalleBase.setLlevent(0);
        detalleBase.setPaguen(0);
        detalleBase.setPorcDctoPlano(0.0);
        detalleBase.setPromocion(promocion);

        promocion.addDetalle(detalleBase);
        return promocion;
    }
}