# Requerimiento de separacion de responsabilidades de microservcios


## Proposito
La finalidad de este documento es definir las necesidades que se tienen actualmente para la implementacion de microservicios y separacion de responsabilidades del proyecto de ms-cotizador en componentes mas pequeños. 

## Situacion actual
Actualmente nuestro proyecto cuenta con 3 aplicaciones que funcionan entre si con un par mas para el funcionamiento de uno de ellos:
- ms-cotizador: Tiene todo para la gestion de cotizacion de componentes de pc. Implementa seguridad por JWT y sesiones
- ms-seguridad: Maneja todo lo relacionado a la seguridad del acceso al portal y a los demas microservicios. . Implementa seguridad por JWT y sesiones
- portal-cotizador: Ofrece una vista web para el usuario, el cual usa para poder llevar a cabo la gestion de todo lo relacionado a las cotizaciones de componentes y armados de pc. Implementa seguridad por JWT y sesiones.
- Bases de datos: Maneja un par de bases de datos para el tema del aplicativo de ms-cotizador y otra para el de seguridad. Cada microservicio
- un api gateway para permitir enrutar de forma correcta las peticiones del portal hacia los micros y poderlos acceder incluso desde una ip externa sin tener que ser localhost siempre
La necesidad que se tiene ahora es separar el proyecto de ms-cotizador para que en lugar de un solo mciro, las funciones y responsabilidades que tiene se dividan en 3, manteniendo la parte de la seguridad igual (el proyecto de ms-seguridad no se tocará. Ese queda igual) pero dividiendo las funciones en distintos micros.

## Metas
La idea es dividir el proyecto de ms-cotizador en otros 3 microservicios para distribuir las funciones que tiene cada uno en modulos mas pequeños. De igual forma, la idea es seguir los principios de diseño de microservicios


## Alcance
El alcance de este prd se limita solo al proyecto de ms-cotizador y a los lugares donde tenga un impacto. En este caso sería el portal web y el api gateway ya que estos interactuan entre si.

## Caracteristicas o Features a implementar
1. ms-cotizador-componentes: Tendria todo lo relacionado a componentes, partes de pc y promociones como se puede ver en el archivo de analisis_dependencias_componentes.md
2. ms-cotizador-cotizaciones: Tendrá todo lo relacionado a las cotizaciones.
3. ms-cotizador-pedidos: Tendrá lo relacionado al manejo de proveedores y pedidos.
4. El proyecto original de ms-cotizador debe desaparecer ya que este se debidira en 3 microservicios mas
5. Basado en el analisis de los documentos analisis_dependencias_componentes.md (ms-cotizador-componentes), analisis_dependencias_cotizaciones.md (ms-cotizador-cotizaciones) y analisis_dependencias_pedidos_y_proveedores.md (ms-cotizador-pedidos) se deben separar las clases por micro y de igualmente cada micro tendrá su propia base de datos
6. Debido a que cada microservicio tendra su propia base de datos y en caso de necesitar info que otro micro tenga en su base, esta se puede repetir sin tema. Para ello se requiere implementar un apache kafka o algun replicador para que cuando se actualice la info en un punto y se requiera en otros, se vaya y se actualice de forma inmediata.
7. Una vez implementado los cambios, se requiere ajustar su invocación desde el portal web para que vaya y se consuma desde el microservicio nuevo que le corresponda


## Stack Tecnologico
El stack que actualmente tiene el proyecto es el siguiente
- Los microservicios tienen lo siguiente:
    - Spring boot 3.5.3
    - Java 21
    - Maven 3.8+
-La pagina usa:
    - Node.js 18+
    - npm 8+
    - Vue 3+
    - Vite
    - Pinia
    - Tailwindcss
- El Api gateway usa una imagen de docker de nginx basado en alpine

## Criterios de aceptacion
Los criterios de aceptacion de la solución a proponer serían los siguientes:
- Separacion del proyecto ms-cotizador en 3 microservicios, cuya funcionalidad migrada siga funcionando igual a como lo hacia todo junto
- El portal de la pagina de cotizador debe seguir funcionando de forma transparente
- Cada microservicio debe tener su propia base de datos con la estructura de la base que le corresponda
- La parte de seguridad deber seguir funcionando igual a como lo hace actualmente el de ms-cotizador (JWT, control de sesiones, etc)
- En caso de que existan tablas redundantes entre bases de datos, estos deben mantenerse actualizados, incluos una vez que los datos en la base original hayan sido modificados o insertados.
- Los Readme.md y documentacion de las clases debe mantenerse actualizadas y hacerse

## Restricciones y dependencias
Los microservicios nuevos deben seguir la misma linea de stack tecnologico que el microservcio de ms-cotizador por lo que no pueden implementarse otras tecnologias distintas.

## Avance
Para el registro del avance, debemos llevar un control de lo que se hace y la fecha y hora en la que se realiza con zona horario America/Mazatlan

## Dudas y comentarios
1. Sobre la estrategia de replicación de datos:

- Mencionas Apache Kafka para la sincronización de tablas redundantes. ¿Prefieres usar Kafka específicamente o estás abierto a otras opciones como:
- Event Sourcing con RabbitMQ
- Change Data Capture (CDC) con Debezium
- Simple webhooks HTTP entre microservicios
- ¿Qué nivel de consistencia necesitas? ¿Eventual consistency está bien o necesitas strong consistency?

1. Me gustaría usar apache kafka original (apache/kafka:4.0.0) y si es necesario usar algun conector para sensar cambios en las tablas de la base de datos ,adelante. Eventual Consistency está bien.

2. Sobre las bases de datos segregadas:

- Según el análisis, hay dependencias fuertes entre módulos. Por ejemplo:
- Cotizaciones necesita datos de Componentes
- Pedidos necesita datos de Cotizaciones y Proveedores
- ¿Cómo quieres manejar estas dependencias? ¿Duplicar tablas o hacer llamadas síncronas entre microservicios?

2. Duplicar tablas me parece bien. De hecho, la idea de usar apache kafka es para mantener la info actualizad entre las tablas duplicadas entre las distintas bases de datos de los micro

3. Sobre el API Gateway:

- ¿Los nuevos microservicios deben exponerse con rutas como:
- /api/componentes/ → ms-cotizador-componentes
- /api/cotizaciones/ → ms-cotizador-cotizaciones
- /api/pedidos/ → ms-cotizador-pedidos
- ¿O prefieres mantener /api/cotizador/ y hacer routing interno basado en el recurso?

3. Me parece bien la primera opción que planteas (/api/componentes).

4. Sobre la migración:

- ¿Prefieres una migración Big Bang (todo de una vez) o gradual (microservicio por microservicio)?
- ¿Hay algún downtime permitido o debe ser zero-downtime?

4. De forma gradual me parece bien. Y en cuanto al downtime, a que te refieres?

5. Sobre la seguridad JWT:

- Cada microservicio necesitará validar JWT tokens. ¿Cada uno debe:
- Llamar a ms-seguridad para validar (como hace ms-cotizador actual)
- O tener su propia validación con JWKS cacheado?
- ¿Los permisos (RBAC) se validan en cada microservicio o centralizadamente?

5. Es correcto, cada micro debe seguir validando el token y validar con el ms-seguridad la vigencia de la sesión actual del usuario. Los permisos RBAC se validan en cada microservicio

6. Sobre los nombres de contenedores y puertos:

- Los nuevos microservicios usarán puertos internos como:
- ms-cotizador-componentes: 8082
- ms-cotizador-cotizaciones: 8083
- ms-cotizador-pedidos: 8084
- ¿Esto está bien o prefieres otra numeración?

6. Me parece bien los nombres que mencionas y los puertos. Solo validar que no choquen entre si

7. Sobre manejo de transacciones distribuidas:

- Algunos casos de uso cruzan límites de microservicios (ej: crear pedido desde cotización)
- ¿Implementamos patrón SAGA o manejamos con compensaciones simples?

7. Si llegan haber casos de uso que cruzan limmites, el patron SAGA está bien. De hecho esa es la idea de usar Apache kafka para que la info se mantenga sincronizada y disponible igual entre todos los micros.

8. Sobre el frontend:

- El frontend actualmente usa una sola base URL. ¿Prefieres:
- Mantener una sola URL y que el gateway haga el routing
- O configurar múltiples base URLs en el frontend?

8. Que siga manejando una sola URL y que el gateway haga el routing, please.

9. Sobre datos maestros compartidos:

- Tablas como TipoComponente son catálogos que raramente cambian
- ¿Las duplicamos en cada base o creamos un microservicio de datos maestros?

9. Si es necesario duplicarlo, adelante.

10. Sobre la estructura de proyectos:

- ¿Quieres mantener todo en el mismo repositorio (monorepo) o separar cada microservicio en su propio repo?

10. Favor de manejarlo todo en un mismo repo