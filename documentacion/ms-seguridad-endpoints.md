# MS-Seguridad - Documentación de Endpoints

Este documento describe todos los endpoints disponibles en el microservicio `ms-seguridad` para su consumo por otros servicios y aplicaciones frontend.

## 📋 Información General

- **Base URL**: `http://localhost:8081/seguridad/v1/api`
- **Base URL Docker**: `http://ms-seguridad:8081/seguridad/v1/api`
- **Autenticación**: JWT Bearer Token (excepto endpoints públicos)
- **Formato de Respuesta**: JSON
- **Content-Type**: `application/json`

## 🔐 Autenticación

Todos los endpoints protegidos requieren un header de autorización:
```http
Authorization: Bearer <access_token>
```

---

## 📚 Endpoints por Categoría

### 🔑 **1. Autenticación (AuthController)**
Base path: `/auth`

#### POST /auth/login
**Descripción**: Autenticar usuario y obtener tokens JWT

**Acceso**: Público (sin autenticación)

**Request Body**:
```json
{
  "usuario": "admin",
  "password": "admin123"
}
```

**Response 200 OK**:
```json
{
  "accessToken": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "refreshExpiresIn": 86400,
  "usuario": "admin",
  "roles": ["ADMIN"],
  "issuedAt": "2025-07-05T14:30:00Z"
}
```

**Response 401 Unauthorized**:
```json
{
  "error": "invalid_credentials",
  "message": "Credenciales inválidas"
}
```

#### POST /auth/refresh
**Descripción**: Renovar access token usando refresh token

**Acceso**: Público (requiere refresh token válido)

**Request Body**:
```json
{
  "refreshToken": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Response 200 OK**:
```json
{
  "accessToken": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "refreshExpiresIn": 86400,
  "usuario": "admin",
  "roles": ["ADMIN"],
  "issuedAt": "2025-07-05T14:30:00Z"
}
```

#### POST /auth/logout
**Descripción**: Invalidar tokens (añadir a blacklist)

**Acceso**: Requiere autenticación (access token)

**Request Body**:
```json
{
  "accessToken": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Response 200 OK**:
```json
{
  "message": "Logout exitoso",
  "loggedOut": true,
  "timestamp": "2025-07-05T14:30:00Z"
}
```

#### GET /auth/validate
**Descripción**: Validar token actual y obtener información del usuario

**Acceso**: Requiere autenticación (access token)

**Response 200 OK**:
```json
{
  "valid": true,
  "usuario": "admin",
  "roles": ["ADMIN"],
  "expiresAt": "2025-07-05T15:30:00Z",
  "issuedAt": "2025-07-05T14:30:00Z"
}
```

#### GET /auth/health
**Descripción**: Health check del servicio de autenticación

**Acceso**: Público

**Response 200 OK**:
```json
{
  "status": "UP",
  "service": "authentication",
  "timestamp": "2025-07-05T14:30:00Z"
}
```

---

### 🔐 **2. Gestión de Claves (KeyController)**
Base path: `/keys`

#### GET /keys/jwks
**Descripción**: Obtener clave pública en formato JWKS estándar (RFC 7517) para validación de JWT

**Acceso**: Público (sin autenticación)

**Response 200 OK**:
```json
{
  "keys": [
    {
      "kty": "RSA",
      "use": "sig",
      "kid": "f027a60e-efcb-3377-a2b9-31fd6c48541b",
      "alg": "RS256",
      "n": "ldRWgcYQZ1s_jr5vkemY52umaDq0k2TBwLJrDNMJxwuhTYKOmcdWubUqZlexIiw5j3ak3vIdm-89XeZKND5q9P0QM5XbfINKXL5YAaXdHlt-TSy8TtbcIy9k_5h7898kYOvUJmoxyzTjmx3LW-QdqnZCYVorJVWF6COmJyDCFMhYu5_JZnfWgPMRHDIX0y-ygXcJ2rrfFHrIiNxykhgdOOssVzkvoRAeZNZi_b7F1NWtEP16uzE9JHqVpud2t-KN2724QZ9ib_xuqDYJdckqVJ2Kr7slm4uV3jJeZYO_Y9yXzhglDaqHn_M-N0tuF2wEYNw0nyvyXn5z0VwrurA4hQ",
      "e": "AQAB"
    }
  ]
}
```

**Campos JWKS**:
- `kty`: Tipo de clave (RSA)
- `use`: Uso de la clave (sig = signature)
- `kid`: Identificador único de la clave
- `alg`: Algoritmo (RS256)
- `n`: Modulus RSA en Base64URL
- `e`: Exponent RSA en Base64URL (típicamente AQAB = 65537)

#### GET /keys/private
**Descripción**: Obtener clave privada (uso administrativo únicamente)

**Acceso**: Requiere rol ADMIN

**Response 200 OK**:
```json
{
  "privateKey": "-----BEGIN PRIVATE KEY-----\nMIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQC...\n-----END PRIVATE KEY-----",
  "algorithm": "RSA",
  "keySize": "2048",
  "keyFormat": "PEM",
  "generatedAt": "2025-07-05T10:30:00",
  "keyId": "f027a60e-efcb-3377-a2b9-31fd6c48541b",
  "usage": "JWT signing (private key)",
  "retrievedAt": "2025-07-05T14:30:00",
  "warning": "¡INFORMACIÓN SENSIBLE! Esta es la clave privada para firmar JWT. Mantenga segura."
}
```

#### POST /keys/generate
**Descripción**: Generar nuevo par de claves RSA (invalida claves anteriores)

**Acceso**: Requiere rol ADMIN

**Response 200 OK**:
```json
{
  "message": "Nuevas claves RSA generadas exitosamente",
  "status": "success",
  "previousKeyId": "old-key-id",
  "newKeyId": "new-key-id",
  "algorithm": "RSA",
  "keySize": "2048",
  "generatedAt": "2025-07-05T14:30:00",
  "warning": "Claves rotadas. Los tokens firmados con la clave anterior ya no serán válidos."
}
```

#### GET /keys/keypair
**Descripción**: Obtener par completo de claves (pública y privada)

**Acceso**: Requiere rol ADMIN

**Response 200 OK**:
```json
{
  "publicKey": "-----BEGIN PUBLIC KEY-----\nMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA...\n-----END PUBLIC KEY-----",
  "privateKey": "-----BEGIN PRIVATE KEY-----\nMIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQC...\n-----END PRIVATE KEY-----",
  "algorithm": "RSA",
  "keySize": "2048",
  "keyFormat": "PEM",
  "generatedAt": "2025-07-05T10:30:00",
  "keyId": "f027a60e-efcb-3377-a2b9-31fd6c48541b",
  "usage": "JWT signing and verification"
}
```

#### GET /keys/info
**Descripción**: Obtener información de claves sin contenido sensible

**Acceso**: Requiere rol ADMIN

**Response 200 OK**:
```json
{
  "keyId": "f027a60e-efcb-3377-a2b9-31fd6c48541b",
  "algorithm": "RSA",
  "keySize": "2048",
  "keyFormat": "PEM",
  "generatedAt": "2025-07-05T10:30:00",
  "usage": "JWT signature verification",
  "hasPublicKey": true,
  "isValidFormat": true,
  "retrievedAt": "2025-07-05T14:30:00"
}
```

#### GET /keys/health
**Descripción**: Health check del servicio de gestión de claves

**Acceso**: Público

**Response 200 OK**:
```json
{
  "status": "UP",
  "service": "key-management",
  "keysAvailable": true,
  "currentKeyId": "f027a60e-efcb-3377-a2b9-31fd6c48541b",
  "timestamp": "2025-07-05T14:30:00"
}
```

---

### 👥 **3. Gestión de Usuarios (UsuarioController)**
Base path: `/usuarios`

#### GET /usuarios
**Descripción**: Listar todos los usuarios

**Acceso**: Requiere rol ADMIN

**Response 200 OK**:
```json
[
  {
    "idUsuario": 1,
    "usuario": "admin",
    "email": "admin@empresa.com",
    "activo": true,
    "fechaCreacion": "2025-07-05T10:00:00",
    "ultimoAcceso": "2025-07-05T14:30:00",
    "roles": [
      {
        "idRol": 1,
        "nombre": "ADMIN",
        "descripcion": "Administrador del sistema"
      }
    ]
  }
]
```

#### GET /usuarios/{id}
**Descripción**: Obtener usuario por ID

**Acceso**: Requiere rol ADMIN

**Response 200 OK**:
```json
{
  "idUsuario": 1,
  "usuario": "admin",
  "email": "admin@empresa.com",
  "activo": true,
  "fechaCreacion": "2025-07-05T10:00:00",
  "ultimoAcceso": "2025-07-05T14:30:00",
  "roles": [
    {
      "idRol": 1,
      "nombre": "ADMIN",
      "descripcion": "Administrador del sistema"
    }
  ]
}
```

#### POST /usuarios
**Descripción**: Crear nuevo usuario

**Acceso**: Requiere rol ADMIN

**Request Body**:
```json
{
  "usuario": "nuevo_usuario",
  "password": "password123",
  "email": "usuario@empresa.com",
  "activo": true,
  "roles": [1]
}
```

**Response 201 Created**:
```json
{
  "idUsuario": 2,
  "usuario": "nuevo_usuario",
  "email": "usuario@empresa.com",
  "activo": true,
  "fechaCreacion": "2025-07-05T14:30:00",
  "ultimoAcceso": null,
  "roles": [
    {
      "idRol": 1,
      "nombre": "ADMIN",
      "descripcion": "Administrador del sistema"
    }
  ]
}
```

#### PUT /usuarios/{id}
**Descripción**: Actualizar usuario existente

**Acceso**: Requiere rol ADMIN

**Request Body**:
```json
{
  "usuario": "usuario_actualizado",
  "email": "actualizado@empresa.com",
  "activo": true,
  "roles": [1, 2]
}
```

#### DELETE /usuarios/{id}
**Descripción**: Eliminar usuario

**Acceso**: Requiere rol ADMIN

**Response 200 OK**:
```json
{
  "message": "Usuario eliminado exitosamente",
  "eliminado": true,
  "timestamp": "2025-07-05T14:30:00"
}
```

---

### 🎭 **4. Gestión de Roles (RolController)**
Base path: `/roles`

#### GET /roles
**Descripción**: Listar todos los roles

**Acceso**: Requiere rol ADMIN

**Response 200 OK**:
```json
[
  {
    "idRol": 1,
    "nombre": "ADMIN",
    "descripcion": "Administrador del sistema",
    "fechaCreacion": "2025-07-05T10:00:00",
    "activo": true
  },
  {
    "idRol": 2,
    "nombre": "USER",
    "descripcion": "Usuario regular",
    "fechaCreacion": "2025-07-05T10:00:00",
    "activo": true
  }
]
```

#### GET /roles/{id}
**Descripción**: Obtener rol por ID

**Acceso**: Requiere rol ADMIN

#### POST /roles
**Descripción**: Crear nuevo rol

**Acceso**: Requiere rol ADMIN

**Request Body**:
```json
{
  "nombre": "SUPERVISOR",
  "descripcion": "Supervisor de operaciones",
  "activo": true
}
```

#### PUT /roles/{id}
**Descripción**: Actualizar rol existente

**Acceso**: Requiere rol ADMIN

#### DELETE /roles/{id}
**Descripción**: Eliminar rol

**Acceso**: Requiere rol ADMIN

---

## 🚀 Ejemplos de Uso para Otros Servicios

### Validación de JWT en otro microservicio

```javascript
// 1. Obtener clave pública JWKS
const response = await fetch('http://ms-seguridad:8081/seguridad/v1/api/keys/jwks');
const jwks = await response.json();

// 2. Usar biblioteca JWT para validar tokens
import { verify } from 'jsonwebtoken';
import jwksClient from 'jwks-rsa';

const client = jwksClient({
  jwksUri: 'http://ms-seguridad:8081/seguridad/v1/api/keys/jwks'
});

function getKey(header, callback) {
  client.getSigningKey(header.kid, (err, key) => {
    const signingKey = key.publicKey || key.rsaPublicKey;
    callback(null, signingKey);
  });
}

// Validar token
verify(token, getKey, { algorithms: ['RS256'] }, (err, decoded) => {
  if (err) {
    console.error('Token inválido:', err);
  } else {
    console.log('Usuario autenticado:', decoded.sub);
    console.log('Roles:', decoded.roles);
  }
});
```

### Autenticación desde Frontend

```javascript
// Login
const loginResponse = await fetch('http://localhost:8081/seguridad/v1/api/auth/login', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    usuario: 'admin',
    password: 'admin123'
  })
});

const tokens = await loginResponse.json();
localStorage.setItem('accessToken', tokens.accessToken);
localStorage.setItem('refreshToken', tokens.refreshToken);

// Usar token en requests posteriores
const protectedResponse = await fetch('http://localhost:8081/seguridad/v1/api/usuarios', {
  headers: {
    'Authorization': `Bearer ${localStorage.getItem('accessToken')}`
  }
});
```

### Refresh Token automático

```javascript
async function refreshTokenIfNeeded() {
  const refreshToken = localStorage.getItem('refreshToken');
  
  const response = await fetch('http://localhost:8081/seguridad/v1/api/auth/refresh', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      refreshToken: refreshToken
    })
  });
  
  if (response.ok) {
    const tokens = await response.json();
    localStorage.setItem('accessToken', tokens.accessToken);
    localStorage.setItem('refreshToken', tokens.refreshToken);
    return tokens.accessToken;
  } else {
    // Redirect to login
    window.location.href = '/login';
  }
}
```

---

## ⚠️ Códigos de Error Comunes

### 401 Unauthorized
```json
{
  "error": "unauthorized",
  "message": "Token requerido",
  "timestamp": "2025-07-05T14:30:00Z"
}
```

### 403 Forbidden
```json
{
  "error": "access_denied",
  "message": "Permisos insuficientes",
  "timestamp": "2025-07-05T14:30:00Z"
}
```

### 400 Bad Request
```json
{
  "error": "validation_error",
  "message": "Datos de entrada inválidos",
  "details": ["El campo 'usuario' es requerido"],
  "timestamp": "2025-07-05T14:30:00Z"
}
```

### 500 Internal Server Error
```json
{
  "error": "internal_error",
  "message": "Error interno del servidor",
  "timestamp": "2025-07-05T14:30:00Z"
}
```

---

## 🔧 Configuración de Entorno

### Variables de Entorno Requeridas

```bash
# Base de datos
DB_HOST=mysql
DB_USERNAME=seguridad_user
DB_PASSWORD=seguridad_pass

# Configuración JWT
JWT_ACCESS_TOKEN_EXPIRATION=3600000    # 1 hora
JWT_REFRESH_TOKEN_EXPIRATION=86400000  # 24 horas
```

### Health Checks

Verificar que el servicio esté funcionando:
```bash
curl http://localhost:8081/seguridad/v1/api/auth/health
curl http://localhost:8081/seguridad/v1/api/keys/health
```

---

## 📝 Notas Importantes

1. **Formato JWKS**: El endpoint `/keys/jwks` sigue el estándar RFC 7517 para máxima compatibilidad
2. **Rotación de Claves**: Al generar nuevas claves, todos los tokens existentes se invalidan
3. **Blacklist**: Los tokens de logout se añaden a una blacklist para prevenir reutilización
4. **Roles**: Los roles se incluyen automáticamente en los JWT claims
5. **Rate Limiting**: Se recomienda implementar rate limiting en el proxy/gateway
6. **HTTPS**: En producción, siempre usar HTTPS para proteger los tokens
7. **Expiración**: Los access tokens tienen corta duración, usar refresh tokens para renovación

---

**Versión del documento**: 1.0  
**Fecha de actualización**: 2025-07-05  
**Microservicio**: ms-seguridad v1.0.0