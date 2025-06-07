package mx.com.qtx.cotizadorv1ds.persistencia.repositorios;

import java.util.List;
import java.util.Optional;

import mx.com.qtx.cotizadorv1ds.config.SpringContextProvider;
import mx.com.qtx.cotizadorv1ds.persistencia.entidades.TipoComponente;

/**
 * Clase de prueba para TipoComponenteRepository usando Java puro (sin frameworks de testing)
 */
public class TipoComponenteRepositoryTest {
    
    private TipoComponenteRepositorio tipoComponenteRepository;
    
    public TipoComponenteRepositoryTest() {
        // Inicializar el contexto de Spring si no está inicializado
        SpringContextProvider.initialize();
        
        // Obtener el repositorio del contexto de Spring
        this.tipoComponenteRepository = SpringContextProvider.getBean(TipoComponenteRepositorio.class);
    }
    
    /**
     * Prueba la creación y recuperación de un tipo de componente
     */
    public void testCrearYRecuperarTipoComponente() {
        System.out.println("\n=== Prueba: Crear y Recuperar Tipo de Componente ===");
        
        // Crear un nuevo tipo de componente
        TipoComponente tipoComponente = new TipoComponente();
        tipoComponente.setId((short) 99); // ID de prueba
        tipoComponente.setNombre("PRUEBA_TIPO");
        
        // Guardar el tipo de componente
        TipoComponente tipoGuardado = tipoComponenteRepository.save(tipoComponente);
        System.out.println("Tipo de componente guardado: ID " + tipoGuardado.getId() + 
                         ", Nombre: " + tipoGuardado.getNombre());
        
        // Recuperar el tipo de componente por ID
        Optional<TipoComponente> tipoRecuperado = tipoComponenteRepository.findById(tipoGuardado.getId());
        if (tipoRecuperado.isPresent()) {
            System.out.println("Tipo de componente recuperado: ID " + tipoRecuperado.get().getId() + 
                             ", Nombre: " + tipoRecuperado.get().getNombre());
        } else {
            System.out.println("ERROR: No se pudo recuperar el tipo de componente");
        }
        
        // Eliminar el tipo de componente de prueba
        try {
            tipoComponenteRepository.delete(tipoGuardado);
            System.out.println("Tipo de componente de prueba eliminado correctamente");
        } catch (Exception e) {
            System.out.println("Error al eliminar el tipo de componente de prueba: " + e.getMessage());
        }
    }
    
    /**
     * Prueba la búsqueda de tipos de componente por nombre
     */
    public void testBuscarPorNombre() {
        System.out.println("\n=== Prueba: Buscar Tipo de Componente por Nombre ===");
        
        // Buscar tipo de componente por nombre
        String nombreBusqueda = "MONITOR";
        Optional<TipoComponente> tipoComponenteOpt = tipoComponenteRepository.findByNombreIgnoreCase(nombreBusqueda);
        
        if (tipoComponenteOpt.isPresent()) {
            TipoComponente tipoComponente = tipoComponenteOpt.get();
            System.out.println("Tipo de componente encontrado con nombre '" + nombreBusqueda + "':");
            System.out.println("- ID: " + tipoComponente.getId() + ", Nombre: " + tipoComponente.getNombre());
        } else {
            System.out.println("No se encontró ningún tipo de componente con nombre '" + nombreBusqueda + "'");
        }
    }
    
    /**
     * Prueba listar todos los tipos de componente
     */
    public void testListarTodos() {
        System.out.println("\n=== Prueba: Listar Todos los Tipos de Componente ===");
        
        // Obtener todos los tipos de componente
        List<TipoComponente> todosTipos = tipoComponenteRepository.findAll();
        System.out.println("Total de tipos de componente: " + todosTipos.size());
        
        // Mostrar todos los tipos de componente
        for (TipoComponente tipo : todosTipos) {
            System.out.println("- ID: " + tipo.getId() + ", Nombre: " + tipo.getNombre());
        }
    }
    
    /**
     * Método principal para ejecutar todas las pruebas
     */
    public static void main(String[] args) {
        System.out.println("Iniciando pruebas de TipoComponenteRepository...");
        
        TipoComponenteRepositoryTest test = new TipoComponenteRepositoryTest();
        
        try {
            // Ejecutar las pruebas
            test.testListarTodos(); // Primero listar todos para ver qué hay en la base de datos
            test.testBuscarPorNombre();
            test.testCrearYRecuperarTipoComponente();
            
            System.out.println("\nTodas las pruebas completadas exitosamente.");
        } catch (Exception e) {
            System.err.println("\nError durante las pruebas: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
