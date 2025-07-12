# SessionController - Endpoints Públicos de Gestión de Sesiones

## Descripción

El `SessionController` proporciona endpoints públicos para la gestión de sesiones sin requerir autenticación. Estos endpoints permiten a aplicaciones externas validar sesiones, cerrar sesiones y obtener información básica de sesiones.

## Endpoints Disponibles

### 1. Validar Sesión

**Endpoint:** `GET /session/validate/{sessionId}`

**Descripción:** Valida si una sesión específica está activa y vigente.

**Parámetros:**
- `sessionId` (path): ID único de la sesión a validar

**Respuesta:**
```json
{
  "sessionId": "uuid-de-la-sesion",
  "isActive": true,
  "message": "Sesión válida y activa",
  "timestamp": "2025-01-15T10:30:00",
  "success": true
}
```

**Códigos de Estado:**
- `200 OK`: Validación exitosa
- `400 Bad Request`: ID de sesión inválido
- `500 Internal Server Error`: Error interno

### 2. Cerrar Sesión

**Endpoint:** `POST /session/close/{sessionId}`

**Descripción:** Cierra una sesión específica por su ID.

**Parámetros:**
- `sessionId` (path): ID único de la sesión a cerrar

**Respuesta:**
```json
{
  "sessionId": "uuid-de-la-sesion",
  "closed": true,
  "message": "Sesión cerrada exitosamente",
  "timestamp": "2025-01-15T10:30:00",
  "success": true
}
```

**Códigos de Estado:**
- `200 OK`: Sesión cerrada exitosamente
- `400 Bad Request`: ID de sesión inválido
- `404 Not Found`: Sesión no encontrada
- `500 Internal Server Error`: Error interno

### 3. Información de Sesión

**Endpoint:** `GET /session/info/{sessionId}`

**Descripción:** Obtiene información básica de una sesión sin exponer datos sensibles.

**Parámetros:**
- `sessionId` (path): ID único de la sesión

**Respuesta:**
```json
{
  "sessionId": "uuid-de-la-sesion",
  "isActive": true,
  "startTime": "2025-01-15T10:00:00",
  "endTime": null,
  "duration": "00:30:00",
  "timestamp": "2025-01-15T10:30:00",
  "success": true
}
```

**Códigos de Estado:**
- `200 OK`: Información obtenida exitosamente
- `400 Bad Request`: ID de sesión inválido
- `404 Not Found`: Sesión no encontrada
- `500 Internal Server Error`: Error interno

### 4. Estado del Controlador

**Endpoint:** `GET /session/health`

**Descripción:** Verifica que el controlador de sesiones esté funcionando correctamente.

**Respuesta:**
```json
{
  "status": "UP",
  "controller": "SessionController",
  "service": "ms-seguridad",
  "timestamp": "2025-01-15T10:30:00",
  "stats": {
    "totalActiveSessions": 25,
    "totalSessions": 150,
    "expiredActiveSessions": 2,
    "serviceStatus": "active"
  }
}
```

## Características de Seguridad

### Rate Limiting
- Todos los endpoints están sujetos a rate limiting
- Límites por defecto: 60 requests/minuto, 1000 requests/hora

### Headers de Cache
- **Validación y Cierre**: `Cache-Control: no-cache`
- **Información**: `Cache-Control: max-age=30`
- **Health**: `Cache-Control: max-age=30`

### Logging de Seguridad
- Todas las operaciones son registradas
- Información de sesiones sin datos sensibles
- Alertas para IDs de sesión inválidos

## Ejemplos de Uso

### Validar Sesión con cURL

```bash
curl -X GET "http://localhost:8080/session/validate/abc123-def456-ghi789" \
  -H "Accept: application/json"
```

### Cerrar Sesión con cURL

```bash
curl -X POST "http://localhost:8080/session/close/abc123-def456-ghi789" \
  -H "Accept: application/json"
```

### Obtener Información de Sesión

```bash
curl -X GET "http://localhost:8080/session/info/abc123-def456-ghi789" \
  -H "Accept: application/json"
```

### Verificar Estado del Controlador

```bash
curl -X GET "http://localhost:8080/session/health" \
  -H "Accept: application/json"
```

## Manejo de Errores

### Errores Comunes

1. **ID de Sesión Inválido (400)**
   ```json
   {
     "sessionId": null,
     "message": "ID de sesión inválido",
     "success": false
   }
   ```

2. **Sesión No Encontrada (404)**
   ```json
   {
     "sessionId": "abc123-def456-ghi789",
     "message": "Sesión no encontrada",
     "success": false
   }
   ```

3. **Error Interno (500)**
   ```json
   {
     "sessionId": "abc123-def456-ghi789",
     "message": "Error interno al validar sesión",
     "success": false
   }
   ```

## Notas Importantes

1. **Endpoints Públicos**: Estos endpoints no requieren autenticación
2. **Rate Limiting**: Implementado para prevenir abuso
3. **Seguridad**: No exponen información sensible como tokens o datos de usuario
4. **Logs**: Todas las operaciones son registradas para auditoría
5. **Cache**: Headers apropiados para optimizar performance

## Integración con Aplicaciones Externas

Estos endpoints están diseñados para ser consumidos por:
- Aplicaciones web que necesitan validar sesiones
- Sistemas de monitoreo que requieren verificar estado de sesiones
- Herramientas administrativas para gestión de sesiones
- APIs que necesitan cerrar sesiones por eventos específicos