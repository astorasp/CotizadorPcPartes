package mx.com.qtx.cotizador.servicio.componente;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import mx.com.qtx.cotizador.dto.common.response.ApiResponse;
import mx.com.qtx.cotizador.dto.componente.request.ComponenteCreateRequest;
import mx.com.qtx.cotizador.dto.componente.request.ComponenteUpdateRequest;
import mx.com.qtx.cotizador.dto.componente.response.ComponenteResponse;
import mx.com.qtx.cotizador.entidad.Componente;
import mx.com.qtx.cotizador.entidad.TipoComponente;
import mx.com.qtx.cotizador.repositorio.ComponenteRepositorio;
import mx.com.qtx.cotizador.repositorio.PcPartesRepositorio;
import mx.com.qtx.cotizador.repositorio.PromocionRepositorio;
import mx.com.qtx.cotizador.repositorio.TipoComponenteRepositorio;
import mx.com.qtx.cotizador.util.Errores;

/**
 * Clase de tests unitarios para ComponenteServicio.
 * 
 * Esta clase valida el comportamiento de la capa de servicio para la gestión
 * de componentes, incluyendo operaciones CRUD, validaciones de negocio,
 * manejo de errores y integración con repositorios mediante mocks.
 * 
 * Casos de prueba cubiertos:
 * - Operaciones CRUD: crear, buscar, actualizar, eliminar componentes
 * - Validaciones de entrada: parámetros nulos, vacíos, inválidos
 * - Manejo de errores: recursos no encontrados, duplicados, errores internos
 * - Búsquedas por tipo y filtros
 * - Casos límite y edge cases
 * 
 * @author Sistema de Cotización PC
 * @version 1.0
 * @see ComponenteServicio
 * @see ComponenteRepositorio
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ComponenteServicio - Tests de Capa de Servicio")
class ComponenteServicioTest {

    @Mock
    private ComponenteRepositorio componenteRepositorio;
    
    @Mock
    private PcPartesRepositorio pcPartesRepositorio;
    
    @Mock
    private PromocionRepositorio promocionRepositorio;
    
    @Mock
    private TipoComponenteRepositorio tipoComponenteRepositorio;

    private ComponenteServicio componenteServicio;
    
    // Datos de prueba
    private TipoComponente tipoDiscoDuro;
    private TipoComponente tipoMonitor;
    private TipoComponente tipoTarjetaVideo;
    private TipoComponente tipoPc;
    private Componente componenteEntity;
    private ComponenteCreateRequest crearRequest;
    private ComponenteUpdateRequest actualizarRequest;
    private mx.com.qtx.cotizador.entidad.Promocion promocionRegular;

    /**
     * Configuración inicial para cada test.
     * Configura mocks básicos y datos de prueba reutilizables.
     */
    @BeforeEach
    void setUp() {
        // Configurar tipos de componentes
        tipoDiscoDuro = new TipoComponente();
        tipoDiscoDuro.setId((short) 1);
        tipoDiscoDuro.setNombre("DISCO_DURO");
        
        tipoMonitor = new TipoComponente();
        tipoMonitor.setId((short) 2);
        tipoMonitor.setNombre("MONITOR");
        
        tipoTarjetaVideo = new TipoComponente();
        tipoTarjetaVideo.setId((short) 3);
        tipoTarjetaVideo.setNombre("TARJETA_VIDEO");
        
        tipoPc = new TipoComponente();
        tipoPc.setId((short) 4);
        tipoPc.setNombre("PC");
        
        List<TipoComponente> tipos = Arrays.asList(tipoDiscoDuro, tipoMonitor, tipoTarjetaVideo, tipoPc);
        when(tipoComponenteRepositorio.findAll()).thenReturn(tipos);
        
        // Configurar promoción
        promocionRegular = new mx.com.qtx.cotizador.entidad.Promocion();
        promocionRegular.setIdPromocion(1);
        promocionRegular.setNombre("Regular");
        
        // Inicializar servicio
        componenteServicio = new ComponenteServicio(
            componenteRepositorio, 
            pcPartesRepositorio, 
            promocionRepositorio, 
            tipoComponenteRepositorio
        );
        
        // Configurar entidad de prueba
        componenteEntity = new Componente();
        componenteEntity.setId("COMP001");
        componenteEntity.setDescripcion("Disco SSD Samsung 1TB");
        componenteEntity.setMarca("Samsung");
        componenteEntity.setModelo("980 EVO");
        componenteEntity.setCosto(new BigDecimal("150.00"));
        componenteEntity.setPrecioBase(new BigDecimal("200.00"));
        componenteEntity.setTipoComponente(tipoDiscoDuro);
        componenteEntity.setPromocion(promocionRegular);
        
        // Configurar requests de prueba
        crearRequest = ComponenteCreateRequest.builder()
            .id("COMP001")
            .descripcion("Disco SSD Samsung 1TB")
            .marca("Samsung")
            .modelo("980 EVO")
            .costo(new BigDecimal("150.00"))
            .precioBase(new BigDecimal("200.00"))
            .tipoComponente("DISCO_DURO")
            .capacidadAlm("1TB")
            .build();
            
        actualizarRequest = ComponenteUpdateRequest.builder()
            .descripcion("Disco SSD Samsung 1TB Actualizado")
            .marca("Samsung")
            .modelo("980 EVO Plus")
            .costo(new BigDecimal("160.00"))
            .precioBase(new BigDecimal("220.00"))
            .tipoComponente("DISCO_DURO")
            .capacidadAlm("1TB")
            .build();
    }

    // ==================== TESTS DE VALIDACIONES BÁSICAS ====================

    /**
     * Verifica validación de request nulo en creación.
     * 
     * Given: Request nulo
     * When: Se invoca guardarComponente()
     * Then: Retorna error de validación
     */
    @Test
    @DisplayName("guardarComponente() con request nulo debe retornar error de validación")
    void guardarComponente_conRequestNulo_debeRetornarErrorValidacion() {
        // Given: Request nulo
        
        // When: Se intenta crear componente con request nulo
        ApiResponse<ComponenteResponse> resultado = componenteServicio.guardarComponente(null);
        
        // Then: Error de validación
        assertAll("Validación request nulo",
            () -> assertEquals(Errores.ERROR_DE_VALIDACION.getCodigo(), resultado.getCodigo()),
            () -> assertEquals("Los datos del componente son requeridos", resultado.getMensaje()),
            () -> assertNull(resultado.getDatos()),
            () -> verify(componenteRepositorio, never()).save(any())
        );
    }

    /**
     * Verifica validación de ID duplicado.
     * 
     * Given: Request con ID que ya existe
     * When: Se invoca guardarComponente()
     * Then: Retorna error de recurso existente
     */
    @Test
    @DisplayName("guardarComponente() con ID duplicado debe retornar error recurso existente")
    void guardarComponente_conIdDuplicado_debeRetornarErrorRecursoExistente() {
        // Given: ID ya existe en BD
        when(componenteRepositorio.existsById("COMP001")).thenReturn(true);
        
        // When: Se intenta crear componente con ID duplicado
        ApiResponse<ComponenteResponse> resultado = componenteServicio.guardarComponente(crearRequest);
        
        // Then: Error de recurso existente
        assertAll("Validación ID duplicado",
            () -> assertEquals(Errores.RECURSO_YA_EXISTE.getCodigo(), resultado.getCodigo()),
            () -> assertEquals(Errores.RECURSO_YA_EXISTE.getMensaje(), resultado.getMensaje()),
            () -> assertNull(resultado.getDatos()),
            () -> verify(componenteRepositorio, never()).save(any())
        );
    }

    /**
     * Verifica validación de ID nulo para búsqueda.
     * 
     * Given: ID nulo
     * When: Se invoca buscarComponente()
     * Then: Retorna error de campo requerido
     */
    @Test
    @DisplayName("buscarComponente() con ID nulo debe retornar error campo requerido")
    void buscarComponente_conIdNulo_debeRetornarErrorCampoRequerido() {
        // Given: ID nulo
        
        // When: Se busca con ID nulo
        ApiResponse<ComponenteResponse> resultado = componenteServicio.buscarComponente(null);
        
        // Then: Error de campo requerido
        assertAll("Validación ID nulo",
            () -> assertEquals(Errores.CAMPO_REQUERIDO.getCodigo(), resultado.getCodigo()),
            () -> assertEquals("El ID del componente es requerido", resultado.getMensaje()),
            () -> assertNull(resultado.getDatos()),
            () -> verify(componenteRepositorio, never()).findByIdWithTipoComponente(anyString())
        );
    }

    /**
     * Verifica comportamiento cuando componente no existe.
     * 
     * Given: ID que no existe en BD
     * When: Se invoca buscarComponente()
     * Then: Retorna error de recurso no encontrado
     */
    @Test
    @DisplayName("buscarComponente() con ID inexistente debe retornar error recurso no encontrado")
    void buscarComponente_conIdInexistente_debeRetornarErrorRecursoNoEncontrado() {
        // Given: Componente no existe
        when(componenteRepositorio.findByIdWithTipoComponente("INEXISTENTE")).thenReturn(null);
        
        // When: Se busca componente inexistente
        ApiResponse<ComponenteResponse> resultado = componenteServicio.buscarComponente("INEXISTENTE");
        
        // Then: Error de recurso no encontrado
        assertAll("Componente no encontrado",
            () -> assertEquals(Errores.RECURSO_NO_ENCONTRADO.getCodigo(), resultado.getCodigo()),
            () -> assertEquals(Errores.RECURSO_NO_ENCONTRADO.getMensaje(), resultado.getMensaje()),
            () -> assertNull(resultado.getDatos()),
            () -> verify(componenteRepositorio).findByIdWithTipoComponente("INEXISTENTE")
        );
    }

    /**
     * Verifica validación de ID nulo para actualización.
     * 
     * Given: ID nulo
     * When: Se invoca actualizarComponente()
     * Then: Retorna error de campo requerido
     */
    @Test
    @DisplayName("actualizarComponente() con ID nulo debe retornar error campo requerido")
    void actualizarComponente_conIdNulo_debeRetornarErrorCampoRequerido() {
        // Given: ID nulo
        
        // When: Se intenta actualizar con ID nulo
        ApiResponse<ComponenteResponse> resultado = componenteServicio.actualizarComponente(null, actualizarRequest);
        
        // Then: Error de campo requerido
        assertAll("Validación ID nulo",
            () -> assertEquals(Errores.CAMPO_REQUERIDO.getCodigo(), resultado.getCodigo()),
            () -> assertEquals("El ID del componente es requerido", resultado.getMensaje()),
            () -> assertNull(resultado.getDatos()),
            () -> verify(componenteRepositorio, never()).save(any())
        );
    }

    /**
     * Verifica validación de request nulo para actualización.
     * 
     * Given: Request nulo
     * When: Se invoca actualizarComponente()
     * Then: Retorna error de validación
     */
    @Test
    @DisplayName("actualizarComponente() con request nulo debe retornar error de validación")
    void actualizarComponente_conRequestNulo_debeRetornarErrorValidacion() {
        // Given: Request nulo
        
        // When: Se intenta actualizar con request nulo
        ApiResponse<ComponenteResponse> resultado = componenteServicio.actualizarComponente("COMP001", null);
        
        // Then: Error de validación
        assertAll("Validación request nulo",
            () -> assertEquals(Errores.ERROR_DE_VALIDACION.getCodigo(), resultado.getCodigo()),
            () -> assertEquals("Los datos del componente son requeridos", resultado.getMensaje()),
            () -> assertNull(resultado.getDatos())
        );
    }

    // ==================== TESTS DE ELIMINACIÓN ====================

    /**
     * Verifica eliminación exitosa de componente.
     * 
     * Given: ID válido de componente existente
     * When: Se invoca borrarComponente()
     * Then: Se elimina el componente exitosamente
     */
    @Test
    @DisplayName("borrarComponente() debe eliminar componente exitosamente")
    void borrarComponente_conIdValido_debeEliminarExitosamente() {
        // Given: Componente existe
        when(componenteRepositorio.existsById("COMP001")).thenReturn(true);
        doNothing().when(componenteRepositorio).deleteById("COMP001");
        
        // When: Se elimina el componente
        ApiResponse<Void> resultado = componenteServicio.borrarComponente("COMP001");
        
        // Then: Eliminación exitosa
        assertAll("Eliminación exitosa",
            () -> assertEquals("0", resultado.getCodigo()),
            () -> assertEquals("Componente eliminado exitosamente", resultado.getMensaje()),
            () -> assertNull(resultado.getDatos()),
            () -> verify(componenteRepositorio).deleteById("COMP001")
        );
    }

    /**
     * Verifica validación de ID vacío para eliminación.
     * 
     * Given: ID vacío
     * When: Se invoca borrarComponente()
     * Then: Retorna error de campo requerido
     */
    @Test
    @DisplayName("borrarComponente() con ID vacío debe retornar error campo requerido")
    void borrarComponente_conIdVacio_debeRetornarErrorCampoRequerido() {
        // Given: ID vacío
        
        // When: Se intenta eliminar con ID vacío
        ApiResponse<Void> resultado = componenteServicio.borrarComponente("   ");
        
        // Then: Error de campo requerido
        assertAll("Validación ID vacío",
            () -> assertEquals(Errores.CAMPO_REQUERIDO.getCodigo(), resultado.getCodigo()),
            () -> assertEquals("El ID del componente es requerido", resultado.getMensaje()),
            () -> assertNull(resultado.getDatos()),
            () -> verify(componenteRepositorio, never()).deleteById(anyString())
        );
    }

    /**
     * Verifica comportamiento al eliminar componente no existente.
     * 
     * Given: ID de componente que no existe
     * When: Se invoca borrarComponente()
     * Then: Retorna error de recurso no encontrado
     */
    @Test
    @DisplayName("borrarComponente() con componente inexistente debe retornar error recurso no encontrado")
    void borrarComponente_conComponenteInexistente_debeRetornarErrorRecursoNoEncontrado() {
        // Given: Componente no existe
        when(componenteRepositorio.existsById("INEXISTENTE")).thenReturn(false);
        
        // When: Se intenta eliminar componente inexistente
        ApiResponse<Void> resultado = componenteServicio.borrarComponente("INEXISTENTE");
        
        // Then: Error de recurso no encontrado
        assertAll("Componente inexistente",
            () -> assertEquals(Errores.RECURSO_NO_ENCONTRADO.getCodigo(), resultado.getCodigo()),
            () -> assertEquals(Errores.RECURSO_NO_ENCONTRADO.getMensaje(), resultado.getMensaje()),
            () -> assertNull(resultado.getDatos()),
            () -> verify(componenteRepositorio, never()).deleteById("INEXISTENTE")
        );
    }

    // ==================== TESTS DE OBTENER COMPONENTES ====================

    /**
     * Verifica obtención de todos los componentes con lista vacía.
     * 
     * Given: No hay componentes en BD
     * When: Se invoca obtenerTodosLosComponentes()
     * Then: Retorna lista vacía exitosamente
     */
    @Test
    @DisplayName("obtenerTodosLosComponentes() con BD vacía debe retornar lista vacía")
    void obtenerTodosLosComponentes_conBDVacia_debeRetornarListaVacia() {
        // Given: BD vacía
        when(componenteRepositorio.findAllWithTipoComponente()).thenReturn(Collections.emptyList());
        
        // When: Se obtienen componentes
        ApiResponse<List<ComponenteResponse>> resultado = componenteServicio.obtenerTodosLosComponentes();
        
        // Then: Lista vacía
        assertAll("Lista vacía",
            () -> assertEquals("0", resultado.getCodigo()),
            () -> assertEquals("Consulta exitosa", resultado.getMensaje()),
            () -> assertNotNull(resultado.getDatos()),
            () -> assertThat(resultado.getDatos()).isEmpty()
        );
    }

    /**
     * Verifica búsqueda con tipo de componente nulo.
     * 
     * Given: Tipo nulo
     * When: Se invoca buscarPorTipo()
     * Then: Retorna error de campo requerido
     */
    @Test
    @DisplayName("buscarPorTipo() con tipo nulo debe retornar error campo requerido")
    void buscarPorTipo_conTipoNulo_debeRetornarErrorCampoRequerido() {
        // Given: Tipo nulo
        
        // When: Se busca por tipo nulo
        ApiResponse<List<ComponenteResponse>> resultado = componenteServicio.buscarPorTipo(null);
        
        // Then: Error de campo requerido
        assertAll("Validación tipo nulo",
            () -> assertEquals(Errores.CAMPO_REQUERIDO.getCodigo(), resultado.getCodigo()),
            () -> assertEquals("El tipo de componente es requerido", resultado.getMensaje()),
            () -> assertNull(resultado.getDatos()),
            () -> verify(componenteRepositorio, never()).findByTipoComponenteNombre(anyString())
        );
    }

    /**
     * Verifica búsqueda con tipo vacío.
     * 
     * Given: Tipo vacío
     * When: Se invoca buscarPorTipo()
     * Then: Retorna error de campo requerido
     */
    @Test
    @DisplayName("buscarPorTipo() con tipo vacío debe retornar error campo requerido")
    void buscarPorTipo_conTipoVacio_debeRetornarErrorCampoRequerido() {
        // Given: Tipo vacío
        
        // When: Se busca por tipo vacío
        ApiResponse<List<ComponenteResponse>> resultado = componenteServicio.buscarPorTipo("  ");
        
        // Then: Error de campo requerido
        assertAll("Validación tipo vacío",
            () -> assertEquals(Errores.CAMPO_REQUERIDO.getCodigo(), resultado.getCodigo()),
            () -> assertEquals("El tipo de componente es requerido", resultado.getMensaje()),
            () -> assertNull(resultado.getDatos())
        );
    }

    // ==================== TESTS DE EXISTENCIA DE COMPONENTE ====================

    /**
     * Verifica validación de existencia de componente.
     * 
     * Given: ID de componente existente
     * When: Se invoca existeComponente()
     * Then: Retorna true indicando que existe
     */
    @Test
    @DisplayName("existeComponente() debe retornar true para componente existente")
    void existeComponente_conIdExistente_debeRetornarTrue() {
        // Given: Componente existe
        when(componenteRepositorio.existsById("COMP001")).thenReturn(true);
        
        // When: Se verifica existencia
        ApiResponse<Boolean> resultado = componenteServicio.existeComponente("COMP001");
        
        // Then: Componente existe
        assertAll("Componente existe",
            () -> assertEquals("0", resultado.getCodigo()),
            () -> assertEquals("El componente existe", resultado.getMensaje()),
            () -> assertTrue(resultado.getDatos()),
            () -> verify(componenteRepositorio).existsById("COMP001")
        );
    }

    /**
     * Verifica validación de componente no existente.
     * 
     * Given: ID de componente no existente
     * When: Se invoca existeComponente()
     * Then: Retorna false indicando que no existe
     */
    @Test
    @DisplayName("existeComponente() debe retornar false para componente inexistente")
    void existeComponente_conIdInexistente_debeRetornarFalse() {
        // Given: Componente no existe
        when(componenteRepositorio.existsById("INEXISTENTE")).thenReturn(false);
        
        // When: Se verifica existencia
        ApiResponse<Boolean> resultado = componenteServicio.existeComponente("INEXISTENTE");
        
        // Then: Componente no existe
        assertAll("Componente no existe",
            () -> assertEquals("0", resultado.getCodigo()),
            () -> assertEquals("El componente no existe", resultado.getMensaje()),
            () -> assertFalse(resultado.getDatos()),
            () -> verify(componenteRepositorio).existsById("INEXISTENTE")
        );
    }

    /**
     * Verifica validación de ID nulo para existencia.
     * 
     * Given: ID nulo
     * When: Se invoca existeComponente()
     * Then: Retorna error de campo requerido
     */
    @Test
    @DisplayName("existeComponente() con ID nulo debe retornar error campo requerido")
    void existeComponente_conIdNulo_debeRetornarErrorCampoRequerido() {
        // Given: ID nulo
        
        // When: Se verifica existencia con ID nulo
        ApiResponse<Boolean> resultado = componenteServicio.existeComponente(null);
        
        // Then: Error de campo requerido
        assertAll("Validación ID nulo",
            () -> assertEquals(Errores.CAMPO_REQUERIDO.getCodigo(), resultado.getCodigo()),
            () -> assertEquals("El ID del componente es requerido", resultado.getMensaje()),
            () -> assertNull(resultado.getDatos()),
            () -> verify(componenteRepositorio, never()).existsById(anyString())
        );
    }

    // ==================== TESTS DE MANEJO DE ERRORES ====================

    /**
     * Verifica manejo de excepción durante eliminación de componente.
     * 
     * Given: Exception en repositorio durante eliminación
     * When: Se invoca borrarComponente()
     * Then: Retorna error interno del servicio
     */
    @Test
    @DisplayName("borrarComponente() debe manejar excepciones y retornar error interno")
    void borrarComponente_conExcepcionEnRepositorio_debeRetornarErrorInterno() {
        // Given: Exception en repositorio
        when(componenteRepositorio.existsById(anyString())).thenThrow(new RuntimeException("Error BD"));
        
        // When: Se intenta eliminar componente
        ApiResponse<Void> resultado = componenteServicio.borrarComponente("COMP001");
        
        // Then: Error interno
        assertAll("Manejo de excepción",
            () -> assertEquals(Errores.ERROR_INTERNO_DEL_SERVICIO.getCodigo(), resultado.getCodigo()),
            () -> assertEquals(Errores.ERROR_INTERNO_DEL_SERVICIO.getMensaje(), resultado.getMensaje()),
            () -> assertNull(resultado.getDatos()),
            () -> verify(componenteRepositorio, never()).deleteById(anyString())
        );
    }

    /**
     * Verifica manejo de excepción durante búsqueda de componente.
     * 
     * Given: Exception en repositorio durante búsqueda
     * When: Se invoca buscarComponente()
     * Then: Retorna error interno del servicio
     */
    @Test
    @DisplayName("buscarComponente() debe manejar excepciones y retornar error interno")
    void buscarComponente_conExcepcionEnRepositorio_debeRetornarErrorInterno() {
        // Given: Exception en repositorio
        when(componenteRepositorio.findByIdWithTipoComponente(anyString()))
            .thenThrow(new RuntimeException("Error BD"));
        
        // When: Se intenta buscar componente
        ApiResponse<ComponenteResponse> resultado = componenteServicio.buscarComponente("COMP001");
        
        // Then: Error interno
        assertAll("Manejo de excepción en búsqueda",
            () -> assertEquals(Errores.ERROR_INTERNO_DEL_SERVICIO.getCodigo(), resultado.getCodigo()),
            () -> assertEquals(Errores.ERROR_INTERNO_DEL_SERVICIO.getMensaje(), resultado.getMensaje()),
            () -> assertNull(resultado.getDatos())
        );
    }

    // ==================== TESTS DE MÉTODOS UTILITARIOS ====================

    /**
     * Verifica obtención de tipos de componentes.
     * 
     * Given: Servicio inicializado con tipos
     * When: Se invoca obtenerTipos()
     * Then: Retorna lista de tipos disponibles
     */
    @Test
    @DisplayName("obtenerTipos() debe retornar lista de tipos de componentes")
    void obtenerTipos_debeRetornarListaDeTipos() {
        // Given: Servicio inicializado con tipos
        
        // When: Se obtienen los tipos
        List<TipoComponente> tipos = componenteServicio.obtenerTipos();
        
        // Then: Lista de tipos disponible
        assertAll("Tipos de componentes",
            () -> assertNotNull(tipos, "Lista de tipos no debe ser nula"),
            () -> assertThat(tipos).hasSize(4),
            () -> assertThat(tipos).extracting(TipoComponente::getNombre)
                .containsExactlyInAnyOrder("DISCO_DURO", "MONITOR", "TARJETA_VIDEO", "PC")
        );
    }

    // ==================== TESTS DE CASOS LÍMITE ====================

    /**
     * Verifica comportamiento con ID con espacios en blanco.
     * 
     * Given: ID con solo espacios
     * When: Se invoca buscarComponente()
     * Then: Retorna error de campo requerido
     */
    @Test
    @DisplayName("buscarComponente() con ID solo espacios debe retornar error campo requerido")
    void buscarComponente_conIdSoloEspacios_debeRetornarErrorCampoRequerido() {
        // Given: ID con solo espacios
        
        // When: Se busca con ID de solo espacios
        ApiResponse<ComponenteResponse> resultado = componenteServicio.buscarComponente("   ");
        
        // Then: Error de campo requerido
        assertAll("Validación ID espacios",
            () -> assertEquals(Errores.CAMPO_REQUERIDO.getCodigo(), resultado.getCodigo()),
            () -> assertEquals("El ID del componente es requerido", resultado.getMensaje()),
            () -> assertNull(resultado.getDatos())
        );
    }

    /**
     * Verifica comportamiento con ID vacío en eliminación.
     * 
     * Given: ID vacío ""
     * When: Se invoca borrarComponente()
     * Then: Retorna error de campo requerido
     */
    @Test
    @DisplayName("borrarComponente() con ID vacío debe retornar error campo requerido")
    void borrarComponente_conIdVacioString_debeRetornarErrorCampoRequerido() {
        // Given: ID vacío
        
        // When: Se intenta eliminar con ID vacío
        ApiResponse<Void> resultado = componenteServicio.borrarComponente("");
        
        // Then: Error de campo requerido
        assertAll("Validación ID vacío string",
            () -> assertEquals(Errores.CAMPO_REQUERIDO.getCodigo(), resultado.getCodigo()),
            () -> assertEquals("El ID del componente es requerido", resultado.getMensaje()),
            () -> assertNull(resultado.getDatos())
        );
    }
}