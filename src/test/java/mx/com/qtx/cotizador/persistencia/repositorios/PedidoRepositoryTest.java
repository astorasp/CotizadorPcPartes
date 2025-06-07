package mx.com.qtx.cotizadorv1ds.persistencia.repositorios;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import mx.com.qtx.cotizadorv1ds.config.SpringContextProvider;
import mx.com.qtx.cotizadorv1ds.persistencia.entidades.Pedido;
import mx.com.qtx.cotizadorv1ds.persistencia.entidades.Componente;
import mx.com.qtx.cotizadorv1ds.persistencia.entidades.Proveedor;

/**
 * Clase de prueba para PedidoRepository usando Java puro (sin frameworks de testing)
 */
public class PedidoRepositoryTest {
    
    private PedidoRepositorio pedidoRepository;
    private ComponenteRepositorio componenteRepository;
    private ProveedorRepositorio proveedorRepository;
    
    public PedidoRepositoryTest() {
        // Inicializar el contexto de Spring si no está inicializado
        SpringContextProvider.initialize();
        
        // Obtener los repositorios del contexto de Spring
        this.pedidoRepository = SpringContextProvider.getBean(PedidoRepositorio.class);
        this.componenteRepository = SpringContextProvider.getBean(ComponenteRepositorio.class);
        this.proveedorRepository = SpringContextProvider.getBean(ProveedorRepositorio.class);
    }
    
    /**
     * Prueba la creación y recuperación de un pedido
     */
    public void testCrearYRecuperarPedido() {
        System.out.println("\n=== Prueba: Crear y Recuperar Pedido ===");
        
        // Obtener un proveedor existente para asociarlo al pedido
        Optional<Proveedor> proveedorOpt = proveedorRepository.findById("TECH001"); // TechSupply SA
        if (!proveedorOpt.isPresent()) {
            System.out.println("ERROR: No se encontró el proveedor con ID 1");
            return;
        }
        Proveedor proveedor = proveedorOpt.get();
        System.out.println("Proveedor seleccionado: " + proveedor.getNombre());
        
        // Crear un nuevo pedido
        Pedido pedido = new Pedido();
        pedido.setProveedor(proveedor); // Asignar el objeto Proveedor completo
        LocalDate ahora = LocalDate.now();
        pedido.setFechaEmision(ahora);
        pedido.setFechaEntrega(ahora.plusDays(14)); // 2 semanas después
        pedido.setNivelSurtido(0); // Nivel de surtido inicial
        
        // Guardar el pedido
        Pedido pedidoGuardado = pedidoRepository.save(pedido);
        System.out.println("Pedido guardado: Num " + pedidoGuardado.getNumPedido() + 
                         ", Fecha emisión: " + pedidoGuardado.getFechaEmision() + 
                         ", Fecha entrega: " + pedidoGuardado.getFechaEntrega() +
                         ", Nivel surtido: " + pedidoGuardado.getNivelSurtido());
        
        // Recuperar el pedido por ID
        Optional<Pedido> pedidoRecuperado = pedidoRepository.findById(pedidoGuardado.getNumPedido());
        if (pedidoRecuperado.isPresent()) {
            System.out.println("Pedido recuperado: Num " + pedidoRecuperado.get().getNumPedido() + 
                             ", Fecha entrega: " + pedidoRecuperado.get().getFechaEntrega() +
                             ", Nivel surtido: " + pedidoRecuperado.get().getNivelSurtido() +
                             ", Proveedor: " + pedidoRecuperado.get().getProveedor().getNombre());
        } else {
            System.out.println("ERROR: No se pudo recuperar el pedido con Num " + pedidoGuardado.getNumPedido());
        }
    }
    
    /**
     * Prueba la búsqueda de pedidos por proveedor
     */
    public void testBuscarPorProveedor() {
        System.out.println("\n=== Prueba: Buscar por Proveedor ===");
        
        // Obtener un proveedor para la búsqueda
        Optional<Proveedor> proveedorOpt = proveedorRepository.findById("TECH001"); // TechSupply SA
        if (!proveedorOpt.isPresent()) {
            System.out.println("ERROR: No se encontró el proveedor con CVE TECH001");
            return;
        }
        Proveedor proveedor = proveedorOpt.get();
        System.out.println("Buscando pedidos del proveedor: " + proveedor.getNombre());
        
        // Buscar pedidos por proveedor
        List<Pedido> pedidos = pedidoRepository.findByProveedorCve(proveedor.getCve());
        System.out.println("Pedidos encontrados del proveedor " + proveedor.getNombre() + ": " + pedidos.size());
        
        // Mostrar los pedidos encontrados
        for (Pedido ped : pedidos) {
            System.out.println("- Num: " + ped.getNumPedido() + ", Fecha Emisión: " + ped.getFechaEmision() + ", Nivel Surtido: " + ped.getNivelSurtido());
        }
    }
    
    /**
     * Prueba la búsqueda de pedidos por rango de fechas
     */
    public void testBuscarPorRangoFechas() {
        System.out.println("\n=== Prueba: Buscar por Rango de Fechas ===");
        
        // Definir rango de fechas para la búsqueda
        LocalDate inicio = LocalDate.now().minusDays(30);
        LocalDate fin = LocalDate.now();
        
        // Buscar pedidos en el rango de fechas
        List<Pedido> pedidos = pedidoRepository.findByFechaEmisionBetween(inicio, fin);
        System.out.println("Pedidos encontrados entre " + inicio + " y " + fin + ": " + pedidos.size());
        
        // Mostrar los pedidos encontrados
        for (Pedido ped : pedidos) {
            System.out.println("- Num: " + ped.getNumPedido() + ", Fecha Emisión: " + ped.getFechaEmision() + ", Nivel Surtido: " + ped.getNivelSurtido());
        }
    }
    
    /**
     * Prueba la búsqueda de pedidos con nivel de surtido
     */
    public void testBuscarPorNivelSurtido() {
        System.out.println("\n=== Prueba: Buscar por Nivel de Surtido ===");
        
        // Definir nivel de surtido para la búsqueda
        Integer nivel = 1;
        
        // Buscar pedidos con el nivel de surtido especificado
        List<Pedido> pedidos = pedidoRepository.findByNivelSurtido(nivel);
        System.out.println("Pedidos encontrados con nivel de surtido " + nivel + ": " + pedidos.size());
        
        // Mostrar los pedidos encontrados
        for (Pedido ped : pedidos) {
            System.out.println("- Num: " + ped.getNumPedido() + ", Fecha Emisión: " + ped.getFechaEmision() + ", Fecha Entrega: " + ped.getFechaEntrega());
        }
    }
    
    /**
     * Prueba la búsqueda de pedidos por rango de fechas de entrega
     */
    public void testBuscarPorRangoFechasEntrega() {
        System.out.println("\n=== Prueba: Buscar por Rango de Fechas de Entrega ===");
        
        // Definir rango de fechas para la búsqueda
        LocalDate inicioEntrega = LocalDate.now();
        LocalDate finEntrega = LocalDate.now().plusMonths(1);
        
        // Buscar pedidos en el rango de fechas de entrega
        List<Pedido> pedidos = pedidoRepository.findByFechaEntregaBetween(inicioEntrega, finEntrega);
        System.out.println("Pedidos encontrados con fecha de entrega entre " + inicioEntrega + " y " + finEntrega + ": " + pedidos.size());
        
        // Mostrar los pedidos encontrados
        for (Pedido ped : pedidos) {
            System.out.println("- Num: " + ped.getNumPedido() + ", Fecha Emisión: " + ped.getFechaEmision() + ", Fecha Entrega: " + ped.getFechaEntrega());
        }
    }
    
    /**
     * Prueba la búsqueda de pedidos que contienen un componente específico
     */
    public void testBuscarPorComponente() {
        System.out.println("\n=== Prueba: Buscar por Componente ===");
        
        // Obtener primer componente disponible
        List<Componente> componentes = componenteRepository.findAll();
        if (componentes.isEmpty()) {
            System.out.println("No hay componentes disponibles para realizar esta prueba");
            return;
        }
        
        String idComponente = componentes.get(0).getId();
        System.out.println("Buscando pedidos con el componente ID: " + idComponente);
        
        // Buscar pedidos con el componente
        List<Pedido> pedidos = pedidoRepository.findPedidosByComponente(idComponente);
        System.out.println("Pedidos encontrados con el componente " + idComponente + ": " + pedidos.size());
        
        // Mostrar los pedidos encontrados
        for (Pedido ped : pedidos) {
            System.out.println("- Num: " + ped.getNumPedido() + ", Fecha Emisión: " + ped.getFechaEmision() + ", Nivel Surtido: " + ped.getNivelSurtido());
        }
    }
    
    /**
     * Prueba listar todos los pedidos
     */
    public void testListarTodos() {
        System.out.println("\n=== Prueba: Listar Todos los Pedidos ===");
        
        // Obtener todos los pedidos
        List<Pedido> todosPedidos = pedidoRepository.findAll();
        System.out.println("Total de pedidos: " + todosPedidos.size());
        
        // Mostrar los primeros 5 pedidos (o menos si hay menos de 5)
        int limite = Math.min(5, todosPedidos.size());
        for (int i = 0; i < limite; i++) {
            Pedido ped = todosPedidos.get(i);
            System.out.println("- Num: " + ped.getNumPedido() + ", Fecha Emisión: " + ped.getFechaEmision() + ", Fecha Entrega: " + ped.getFechaEntrega() + ", Nivel Surtido: " + ped.getNivelSurtido());
        }
        
        if (todosPedidos.size() > 5) {
            System.out.println("... y " + (todosPedidos.size() - 5) + " más");
        }
    }
    
    /**
     * Método principal para ejecutar todas las pruebas
     */
    public static void main(String[] args) {
        System.out.println("Iniciando pruebas de PedidoRepository...");
        
        PedidoRepositoryTest test = new PedidoRepositoryTest();
        
        try {
            // Ejecutar las pruebas
            test.testCrearYRecuperarPedido();
            test.testBuscarPorProveedor();
            test.testBuscarPorRangoFechas();
            test.testBuscarPorNivelSurtido();
            test.testBuscarPorRangoFechasEntrega();
            test.testBuscarPorComponente();
            test.testListarTodos();
            
            System.out.println("\nTodas las pruebas completadas exitosamente.");
        } catch (Exception e) {
            System.err.println("\nError durante las pruebas: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
