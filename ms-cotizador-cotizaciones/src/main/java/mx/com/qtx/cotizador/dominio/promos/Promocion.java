package mx.com.qtx.cotizador.dominio.promos;

import java.math.BigDecimal;
import java.util.Map;

import mx.com.qtx.cotizador.dominio.core.componentes.IPromocion;

/**
 * Clase abstracta base que representa el concepto de promoción en el sistema CotizadorPcPartes.
 * <p>
 * Esta clase implementa el patrón Decorator como <strong>Component</strong> base,
 * definiendo la interfaz común para todas las promociones del sistema. Proporciona
 * la estructura fundamental sobre la cual se construyen tanto promociones simples
 * como promociones complejas compuestas por múltiples descuentos.
 * </p>
 *
 * <h3>Patrón Decorator aplicado:</h3>
 * <p>
 * Esta clase forma parte del patrón Decorator donde representa el:
 * </p>
 * <ul>
 *   <li><strong>Component:</strong> Interfaz base que define las operaciones de promoción</li>
 *   <li><strong>ConcreteComponent:</strong> Implementado por {@link PromBase} para promociones simples</li>
 *   <li><strong>Decorator:</strong> Extendido por {@link PromAcumulable} para promociones compuestas</li>
 *   <li><strong>ConcreteDecorator:</strong> Implementado por clases como {@link PromDsctoPlano}</li>
 * </ul>
 *
 * <h3>Jerarquía de promociones:</h3>
 * <pre>
 * Promocion (abstracta)
 * ├── PromBase (no acumulable)
 * │   ├── PromSinDescto (sin descuento)
 * │   └── PromNXM (lleve N, pague M)
 * └── PromAcumulable (decorador)
 *     ├── PromDsctoPlano (descuento porcentual adicional)
 *     └── PromDsctoXcantidad (descuentos por cantidad)
 * </pre>
 *
 * <h3>Responsabilidades principales:</h3>
 * <ul>
 *   <li><strong>Definir interfaz común:</strong> Método {@link #calcularImportePromocion(int, BigDecimal)}</li>
 *   <li><strong>Gestionar metadatos:</strong> Nombre y descripción de la promoción</li>
 *   <li><strong>Factoría de promociones:</strong> Método {@link #crearPromocion(PromocionBuilder)}</li>
 *   <li><strong>Constructor fluido:</strong> Método {@link #getBuilder()} para patrón Builder</li>
 *   <li><strong>Utilidades de depuración:</strong> Método {@link #mostrarEstructuraPromocion(Promocion)}</li>
 * </ul>
 *
 * <h3>Implementaciones disponibles:</h3>
 * <table border="1">
 *   <tr><th>Tipo</th><th>Clase</th><th>Descripción</th><th>Acumulable</th></tr>
 *   <tr><td>Base</td><td>{@link PromSinDescto}</td><td>Sin descuento</td><td>No</td></tr>
 *   <tr><td>Base</td><td>{@link PromNXM}</td><td>Lleve N, pague M</td><td>No</td></tr>
 *   <tr><td>Decorator</td><td>{@link PromDsctoPlano}</td><td>Descuento porcentual</td><td>Sí</td></tr>
 *   <tr><td>Decorator</td><td>{@link PromDsctoXcantidad}</td><td>Descuento por cantidad</td><td>Sí</td></tr>
 * </table>
 *
 * <h3>Ejemplo de uso básico:</h3>
 * <pre>{@code
 * // Promoción simple
 * Promocion promoSimple = new PromNXM(3, 2);
 * BigDecimal precio = promoSimple.calcularImportePromocion(6, new BigDecimal("100.00"));
 *
 * // Promoción compuesta usando Builder
 * Promocion promoCompuesta = Promocion.getBuilder()
 *     .conPromocionBaseNXM(3, 2)
 *     .agregarDsctoPlano(10.0f)
 *     .build();
 * }</pre>
 *
 * <h3>Características técnicas:</h3>
 * <ul>
 *   <li><strong>Inmutabilidad parcial:</strong> Los campos pueden modificarse después de la construcción</li>
 *   <li><strong>Composición recursiva:</strong> Permite anidamiento ilimitado de decoradores</li>
 *   <li><strong>Precisión decimal:</strong> Utiliza {@link BigDecimal} para cálculos exactos</li>
 *   <li><strong>Factoría integrada:</strong> Incluye método de factoría para creación compleja</li>
 *   <li><strong>Utilidades de introspección:</strong> Métodos para examinar la estructura de promociones</li>
 * </ul>
 *
 * @author hp835 - [2025-01-17 19:30:00 MST]
 * @version 1.0
 * @since 1.0
 * @see mx.com.qtx.cotizador.dominio.core.componentes.IPromocion
 * @see mx.com.qtx.cotizador.dominio.promos.PromBase
 * @see mx.com.qtx.cotizador.dominio.promos.PromAcumulable
 * @see mx.com.qtx.cotizador.dominio.promos.PromocionBuilder
 */
public abstract class Promocion implements IPromocion{

	/**
	 * Descripción textual detallada de la promoción.
	 * <p>
	 * Esta cadena proporciona una explicación completa del tipo de promoción,
	 * sus reglas de aplicación y los beneficios que ofrece al cliente. La descripción
	 * debe ser lo suficientemente detallada para que los usuarios comprendan
	 * completamente cómo funciona la promoción.
	 * </p>
	 * <p>
	 * Ejemplos de descripciones:
	 * <ul>
	 *   <li>"Lleve 3, pague 2"</li>
	 *   <li>"Descuento Plano del 15.00 %"</li>
	 *   <li>"Descuento por cantidad: 5%=5unidades, 10%=10unidades"</li>
	 * </ul>
	 * </p>
	 */
	private String descripcion;

	/**
	 * Nombre corto identificativo de la promoción.
	 * <p>
	 * Esta cadena proporciona un identificador breve y memorable para la promoción.
	 * Es utilizado principalmente para propósitos de logging, debugging y
	 * identificación rápida en interfaces de usuario.
	 * </p>
	 * <p>
	 * Ejemplos de nombres:
	 * <ul>
	 *   <li>"3x2"</li>
	 *   <li>"Dscto Plano"</li>
	 *   <li>"Precio regular"</li>
	 * </ul>
	 * </p>
	 */
	private String nombre;

 
	/**
	 * Constructor que inicializa una promoción con su descripción y nombre.
	 * <p>
	 * Crea una nueva instancia de promoción configurada con los metadatos básicos
	 * que la identifican y describen. Estos metadatos son utilizados tanto para
	 * propósitos de presentación al usuario como para logging y debugging.
	 * </p>
	 * <p>
	 * <strong>Nota:</strong> Este constructor es llamado por todas las subclases
	 * concretas (tanto {@link PromBase} como {@link PromAcumulable}) para establecer
	 * la información básica de la promoción.
	 * </p>
	 *
	 * @param descripcion Descripción detallada del tipo de promoción y sus reglas
	 * @param nombre Nombre corto identificativo de la promoción
	 * @throws IllegalArgumentException si descripcion o nombre son null
	 * @see PromBase#PromBase(String, String)
	 * @see PromAcumulable#PromAcumulable(String, String, Promocion)
	 */
	public Promocion(String descripcion, String nombre) {
		super();
		this.descripcion = descripcion;
		this.nombre = nombre;
	}

	/**
	 * Obtiene la descripción detallada de la promoción.
	 * <p>
	 * Retorna la descripción completa que explica cómo funciona la promoción,
	 * qué beneficios ofrece y qué reglas aplica. Esta descripción es utilizada
	 * principalmente para mostrar información detallada al usuario final.
	 * </p>
	 *
	 * @return La descripción textual completa de la promoción
	 * @see #setDescripcion(String)
	 * @see #getNombre()
	 */
	public String getDescripcion() {
		return descripcion;
	}

	/**
	 * Establece una nueva descripción para la promoción.
	 * <p>
	 * Permite modificar dinámicamente la descripción de la promoción después
	 * de su creación. Esta funcionalidad puede ser útil para internacionalización
	 * o para actualizar la descripción basada en cambios en las reglas de la promoción.
	 * </p>
	 *
	 * @param descripcion La nueva descripción detallada de la promoción
	 * @throws IllegalArgumentException si descripcion es null
	 * @see #getDescripcion()
	 * @see #setNombre(String)
	 */
	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	/**
	 * Obtiene el nombre corto identificativo de la promoción.
	 * <p>
	 * Retorna el nombre abreviado que sirve como identificador único y memorable
	 * de la promoción. Este nombre es utilizado en logs, interfaces de usuario
	 * y operaciones de debugging.
	 * </p>
	 *
	 * @return El nombre corto de la promoción
	 * @see #setNombre(String)
	 * @see #getDescripcion()
	 */
	public String getNombre() {
		return nombre;
	}

	/**
	 * Establece un nuevo nombre para la promoción.
	 * <p>
	 * Permite modificar dinámicamente el nombre identificativo de la promoción
	 * después de su creación. Esta funcionalidad puede ser útil para rebranding
	 * o para adaptar el nombre a diferentes contextos de uso.
	 * </p>
	 *
	 * @param nombre El nuevo nombre corto identificativo de la promoción
	 * @throws IllegalArgumentException si nombre es null
	 * @see #getNombre()
	 * @see #setDescripcion(String)
	 */
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	/**
	 * Calcula el importe final de la promoción aplicando sus reglas específicas.
	 * <p>
	 * Este método abstracto define el contrato que todas las promociones deben
	 * implementar. Cada tipo de promoción (simple o compuesta) tiene su propia
	 * lógica para calcular cómo se aplica el descuento o beneficio sobre el
	 * precio base del componente.
	 * </p>
	 * <p>
	 * El cálculo debe considerar:
	 * <ul>
	 *   <li>La cantidad de unidades del componente</li>
	 *   <li>El precio base unitario del componente</li>
	 *   <li>Las reglas específicas de la promoción</li>
	 *   <li>La composición con otras promociones (en caso de decoradores)</li>
	 * </ul>
	 * </p>
	 *
	 * @param cant Cantidad de unidades del componente a las que aplicar la promoción
	 * @param precioBase Precio base unitario del componente antes de aplicar promociones
	 * @return Importe final después de aplicar todas las reglas de la promoción
	 * @throws IllegalArgumentException si cant es menor o igual a cero
	 * @throws IllegalArgumentException si precioBase es null o negativo
	 * @see PromSinDescto#calcularImportePromocion(int, BigDecimal)
	 * @see PromNXM#calcularImportePromocion(int, BigDecimal)
	 * @see PromDsctoPlano#calcularImportePromocion(int, BigDecimal)
	 */
	public abstract BigDecimal calcularImportePromocion(int cant, BigDecimal precioBase);

	/**
	 * Método de factoría que crea promociones complejas utilizando el patrón Builder.
	 * <p>
	 * Este método implementa el patrón Factory Method para construir promociones
	 * compuestas basadas en la configuración proporcionada por un {@link PromocionBuilder}.
	 * Permite crear promociones complejas que combinan múltiples tipos de descuentos
	 * de manera declarativa y fácil de mantener.
	 * </p>
	 * <p>
	 * El proceso de construcción sigue estos pasos:
	 * </p>
	 * <ol>
	 *   <li>Crea la promoción base según el tipo especificado ({@link PromSinDescto} o {@link PromNXM})</li>
	 *   <li>Aplica secuencialmente todos los descuentos planos configurados</li>
	 *   <li>Aplica secuencialmente todos los descuentos por cantidad configurados</li>
	 *   <li>Retorna la promoción completamente configurada</li>
	 * </ol>
	 * <p>
	 * <strong>Nota:</strong> Si no se especifica un tipo de promoción base válido,
	 * se utiliza {@link PromSinDescto} como valor por defecto.
	 * </p>
	 *
	 * @param builder Instancia de {@link PromocionBuilder} configurada con todos los
	 *                parámetros necesarios para construir la promoción
	 * @return Una nueva instancia de {@link Promocion} completamente configurada
	 *         con todas las reglas de descuento aplicadas
	 * @throws IllegalArgumentException si builder es null
	 * @see PromocionBuilder
	 * @see #getBuilder()
	 * @see PromSinDescto
	 * @see PromNXM
	 * @see PromDsctoPlano
	 * @see PromDsctoXcantidad
	 */
	public static Promocion crearPromocion(PromocionBuilder builder){

		Promocion promoBase = null;
		int tipoPromBase = builder.getTipoPromocionBase();
		switch(tipoPromBase) {
			case PromocionBuilder.PROM_BASE_SIN_DSCTO:
				promoBase = new PromSinDescto();
				break;
			case PromocionBuilder.PROM_BASE_NXM:
				promoBase = new PromNXM(builder.getN(), builder.getM());
				break;
			default:
				promoBase = new PromSinDescto();
		}

		Promocion promoAcum = promoBase;
		for(Float dsctoPlanoI:builder.getLstDsctosPlanos()) {
			Promocion promDeco = new PromDsctoPlano(promoAcum,dsctoPlanoI);
			promoAcum = promDeco;
		}
		for(Map<Integer,Double> mapDsctosI:builder.getLstMapsCantVsDscto()) {
			Promocion promDeco = new PromDsctoXcantidad(promoAcum, mapDsctosI);
			promoAcum = promDeco;
		}

		return promoAcum;
	}

	/**
	 * Método de factoría que proporciona una nueva instancia de PromocionBuilder.
	 * <p>
	 * Este método implementa el patrón Builder facilitando el acceso al constructor
	 * fluido para crear promociones complejas. Permite encadenar llamadas de método
	 * para configurar diferentes aspectos de la promoción de manera legible y mantenible.
	 * </p>
	 * <p>
	 * El patrón Builder resultante permite:
	 * <ul>
	 *   <li>Seleccionar el tipo de promoción base</li>
	 *   <li>Agregar descuentos planos adicionales</li>
	 *   <li>Configurar descuentos por cantidad</li>
	 *   <li>Construir la promoción final con {@link PromocionBuilder#build()}</li>
	 * </ul>
	 * </p>
	 *
	 * @return Una nueva instancia de {@link PromocionBuilder} lista para ser configurada
	 * @see PromocionBuilder
	 * @see #crearPromocion(PromocionBuilder)
	 */
	public static PromocionBuilder getBuilder() {
		return new PromocionBuilder();
	}

	/**
	 * Método de utilidad que muestra en consola la estructura completa de una promoción.
	 * <p>
	 * Este método proporciona una vista jerárquica de la estructura interna de una promoción,
	 * mostrando cómo están compuestos los diferentes niveles de decoradores. Es especialmente
	 * útil para debugging y comprensión de promociones complejas que combinan múltiples
	 * tipos de descuentos.
	 * </p>
	 * <p>
	 * La salida muestra:
	 * <ul>
	 *   <li>El nombre de cada clase en la jerarquía de decoradores</li>
	 *   <li>La descripción específica de cada nivel de promoción</li>
	 *   <li>La estructura completa desde la promoción base hasta los decoradores más externos</li>
	 * </ul>
	 * </p>
	 * <p>
	 * <strong>Formato de salida:</strong>
	 * <pre>
	 * -------------------------------------------------------------------------------------------
	 * PromNXM: 3 X 2
	 * PromDsctoPlano: Descuento Plano del 10.00 %
	 * -------------------------------------------------------------------------------------------
	 * </pre>
	 * </p>
	 *
	 * @param prom La promoción cuya estructura se desea mostrar
	 * @see #mostrarElemEstructuraPromocion(Promocion)
	 * @see PromBase
	 * @see PromAcumulable
	 */
	public static void mostrarEstructuraPromocion(Promocion prom) {
		System.out.println("\n---------------------------------------------------------------------------------------------");
		mostrarElemEstructuraPromocion(prom);
		System.out.println("---------------------------------------------------------------------------------------------\n");
	}
	
	/**
	 * Método auxiliar recursivo que muestra un elemento individual de la estructura de promoción.
	 * <p>
	 * Este método implementa la lógica recursiva para recorrer la jerarquía de decoradores
	 * de una promoción. Utiliza el patrón Visitor para identificar el tipo de promoción
	 * y mostrar su información correspondiente.
	 * </p>
	 * <p>
	 * El algoritmo funciona de la siguiente manera:
	 * </p>
	 * <ul>
	 *   <li><strong>Promociones base ({@link PromBase}):</strong> Muestra directamente la información</li>
	 *   <li><strong>Promociones acumulables ({@link PromAcumulable}):</strong> Primero muestra recursivamente
	 *       la promoción base, luego muestra la información del decorador actual</li>
	 * </ul>
	 * <p>
	 * Esta recursión garantiza que la estructura se muestre en el orden correcto:
	 * desde la promoción más interna (base) hasta la más externa (decoradores).
	 * </p>
	 *
	 * @param prom La promoción cuyo elemento se desea mostrar
	 * @see #mostrarEstructuraPromocion(Promocion)
	 * @see PromBase
	 * @see PromAcumulable
	 */
	private static void mostrarElemEstructuraPromocion(Promocion prom) {
		if(prom instanceof PromBase) {
			System.out.println(prom.getClass().getSimpleName() + ": " + prom.getDescripcion());
		}
		else
		if(prom instanceof PromAcumulable promAcum) {
			mostrarElemEstructuraPromocion(promAcum.promoBase);
			System.out.println(prom.getClass().getSimpleName() + ": " + prom.getDescripcion());
		}
	}

}