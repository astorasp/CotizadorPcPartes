## Requerimiento

### Pasos siguientes
Ademas del desarrollo que ya tiene este proyecto y que puede ser consulta en el archivo de CLAUDE.md es necesario realizar los siguientes pasos ademas de algunas consideraciones adicionales:

1. Crear un nuevo proyecto llamado ms-seguridad donde manejaremos todo lo relacionado a la autenticación de los usuarios y el manejo de JWT.
2. El proyecto ms-seguridad debe manejar el siguiente stack:
    1. Java 21.
    2. Spring boot 3.5.3
    3. Spring Data JPA
    4. Lista para poder interactuar con base de datos de MySQL
    5. Debemos usar las librerias necesarias de jsonwebtoken para generar JWT tokens (jjwt-api, jjwt-impl, jjwt-jackson)
    6. Debe utilizar Spring Security para solo permitir que ciertas urls sean accesibles (como /auth, etc)
3. El proyecto ms-seguridad tendrá las siguientes responsabilidades.
    1. Autenticar a los usuarios recibiendo un usuario y contraseña y validando dicha información contra una base de datos llamada seguridad que estaría basado en MySQL. Considerar que el password estaría hasheado con bcrypt o algo que se le parezca
    2. Generar un access token y un refresh token. El primero debe tener una duración de 10 minutos y el segundo de 2 horas
    3. El token generado debe tener los roles que el usuario tenga asignados en base de datos
    4. Debe exponer un set de endpoints para dar mantenimiento a los usuarios (un CRUD literamente)
    5. Nuestro esquema debe soportar el uso de roles. Estos nos indicarán a que urls estarían permitidos consultar un usuario.
    6. Debe permitir generar el par de llaves que se usarán con el algoritmo de JWT para firmar los tokens. Al arrancar el aplicativo debe generar el primer set y cuando se llame un endpoint, generar un set nuevo.
    7. Generar un endpoint para poder obtener las llaves privada y publica actualmente cargadas
    8. Debe permitir, por medio de un endpoint, generar un nuevo set de llaves
    9. Las llaves no deben guardarse como archivos dentro del contenedor sino en memoria
    10. Generar un endpoint para obtener la vigencia restante del token antes de expirar: hh:mm:ss (hora:minuto:segundo). Si el token ya se venció debe regresar 00:00:00
    11. Considerar que para el algoritmo de firmado usaremos RS256
4. Dockerizar el componente generando un archivo Dockerfile ya que luego se integrará al docker-compose.yml que se encuentra en la raiz del proyecto
5. Considerar que el archivo de docker debe ser multilayer para tratar de que la imagen del contenedor sea lo mas sencillo y limpio posible



# Fase Final (Pendiente de negociar)
Se deben considerar la siguientes cosas:
1. implementar un sistema de seguridad para agregar a una lista negra o blacklist tokens que sepamos que se pueda haber comprometido
2. Exponer un endpoint para agregar o quitar un token del blacklist (¿Preguntar como se abordaría esto a nivel de base de datos o del aplicativo?)
3. Llevar un control de numero de intentos fallidos realizados por el usuario (¿Preguntar como se abordaría esto a nivel de base de datos o del aplicativo?)
4. Implementar un nuevo configurable de intentos antes de bloquear el usuario
5. Al sobrepasar el usuario el numero de intentos maximo, se debe bloquear su cuenta por una cantidad configurable de tiempo en minutos