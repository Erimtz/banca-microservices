# Entrega 1 — Microservicios con Spring Boot 4, Java 21 y Docker

Proyecto de dos microservicios construidos con **Java 21**, **Spring Boot 4.1.2** y
**Docker**, desplegados como **dos contenedores** que se comunican entre sí de forma
síncrona por REST.

---

## 1. Stack tecnológico

| Componente | Versión |
|---|---|
| Java | 21 (Temurin LTS) |
| Spring Boot | 4.1.2 |
| Spring Framework | 7.x (incluido en Boot 4) |
| Maven | 3.9.x |
| Docker / Docker Compose | 28.x / v2.34 |
| Imagen base runtime | `eclipse-temurin:21-jre` |

> Spring Boot 4 exige Java 17 o superior; se usa Java 21, la LTS recomendada.

---

## 2. Arquitectura

```
                 ┌──────────────────────────────────────────────┐
                 │            Red bridge: banca                 │
                 │                                              │
   Cliente HTTP  │   ┌──────────────────┐    HTTP (RestClient)  │
  ──────────────►│   │ cliente-service  │ ────────────────────► │
   :8081         │   │   puerto 8081    │   GET /api/cuentas/   │
                 │   │                  │   cliente/{id}        │
                 │   └──────────────────┘                       │
                 │            ▲                                 │
                 │            │ HTTP (RestClient)               │
                 │            │ GET /api/clientes/{id}          │
                 │            ▼                                 │
                 │   ┌──────────────────┐                       │
                 │   │  cuenta-service  │                       │
                 │   │   puerto 8082    │                       │
                 │   └──────────────────┘                       │
                 └──────────────────────────────────────────────┘
```

La comunicación es **bidireccional**:

| Sentido | Endpoint propio | Endpoint consumido | Cliente HTTP |
|---|---|---|---|
| `cliente-service` → `cuenta-service` | `GET /api/clientes/{id}/cuentas` | `GET /api/cuentas/cliente/{clienteId}` | `CuentasClient` |
| `cuenta-service` → `cliente-service` | `GET /api/cuentas/{id}/titular` | `GET /api/clientes/{id}` | `ClientesClient` |

Cada microservicio consume al otro **por el nombre del servicio dentro de la red de
Docker** (`http://cuenta-service:8082` y `http://cliente-service:8081`), no por `localhost`.
La URL es configurable por variable de entorno, por lo que el mismo JAR funciona fuera
de Docker apuntando a `localhost`.

Los microservicios **no comparten clases**: cada uno define sus propios DTOs
(`CuentaDTO` está duplicado a propósito, igual que `TitularDTO`/`ClienteDTO`). Es el
comportamiento esperado en un sistema desacoplado.

---

## 3. Estructura del proyecto

```
repository/
├── docker-compose.yml          # Levanta los dos contenedores en la misma red
├── postman/                    # Colecciones JSON para probar la API
├── cliente-service/
│   ├── Dockerfile              # Build multi-stage: Maven → JRE
│   ├── pom.xml
│   └── src/
│       ├── main/java/co/edu/poli/banca/cliente/
│       │   ├── ClienteApplication.java
│       │   ├── controller/ClienteController.java
│       │   ├── service/ClienteService.java
│       │   ├── repository/ClienteRepository.java     # en memoria
│       │   ├── client/CuentasClient.java             # consume a cuenta-service
│       │   ├── config/RestClientConfig.java
│       │   ├── dto/                                  # ClienteDTO, CuentaDTO, ...
│       │   └── exception/                            # manejo global de errores
│       └── test/java/...                             # 15 pruebas
└── cuenta-service/
    ├── Dockerfile
    ├── pom.xml
    └── src/
        ├── main/java/co/edu/poli/banca/cuenta/
        │   ├── CuentaApplication.java
        │   ├── controller/CuentaController.java
        │   ├── service/CuentaService.java
        │   ├── repository/CuentaRepository.java      # en memoria
        │   ├── client/ClientesClient.java            # consume a cliente-service
        │   ├── config/RestClientConfig.java
        │   ├── dto/
        │   └── exception/
        └── test/java/...                             # 16 pruebas
```

No hay base de datos: los datos viven en un `ConcurrentHashMap` con información
sembrada al arrancar, de modo que el sistema se mantiene en **exactamente dos
contenedores**.

---

## 4. Cómo ejecutarlo

### 4.1 Con Docker (recomendado)

```bash
docker compose up --build
```

O en segundo plano:

```bash
docker compose up --build -d
docker compose ps          # los dos contenedores deben quedar "healthy"
```

Detener:

```bash
docker compose down
```

### 4.2 Sin Docker (desarrollo local)

Terminal 1:

```bash
cd cuenta-service
mvn spring-boot:run        # escucha en :8082
```

Terminal 2:

```bash
cd cliente-service
mvn spring-boot:run        # escucha en :8081
```

---

## 5. Endpoints

### cliente-service — `http://localhost:8081`

| Método | Ruta | Descripción |
|---|---|---|
| `GET` | `/api/clientes` | Lista todos los clientes |
| `GET` | `/api/clientes/{id}` | Detalle de un cliente |
| `POST` | `/api/clientes` | Crea un cliente (validado) |
| `PUT` | `/api/clientes/{id}` | Actualiza un cliente (validado) |
| `DELETE` | `/api/clientes/{id}` | Elimina un cliente |
| `GET` | `/api/clientes/{id}/cuentas` | **Consume al `cuenta-service`** |
| `GET` | `/actuator/health` | Health check usado por Docker |

### cuenta-service — `http://localhost:8082`

| Método | Ruta | Descripción |
|---|---|---|
| `GET` | `/api/cuentas` | Lista todas las cuentas |
| `GET` | `/api/cuentas/{id}` | Detalle de una cuenta |
| `POST` | `/api/cuentas` | Crea una cuenta (validada) |
| `PUT` | `/api/cuentas/{id}` | Actualiza una cuenta (validada) |
| `DELETE` | `/api/cuentas/{id}` | Elimina una cuenta |
| `GET` | `/api/cuentas/cliente/{clienteId}` | Cuentas de un cliente (lo consume `cliente-service`) |
| `GET` | `/api/cuentas/{id}/titular` | **Consume al `cliente-service`** |
| `GET` | `/actuator/health` | Health check usado por Docker |

---

## 6. Verificando la comunicación entre contenedores

Con `docker compose up` corriendo:

```bash
# 1. cliente-service llama a cuenta-service y devuelve el cliente + sus cuentas
curl http://localhost:8081/api/clientes/1/cuentas

# 2. cuenta-service llama a cliente-service y devuelve la cuenta + su titular
curl http://localhost:8082/api/cuentas/1/titular

# 3. Ver los logs cruzados: se ve la petición HTTP saliendo de cada contenedor
docker compose logs -f
```

Respuesta esperada del primer comando:

```json
{
  "cliente": {
    "id": 1,
    "nombre": "Ana Gómez",
    "email": "ana.gomez@ejemplo.com",
    "documento": "1023456789"
  },
  "cuentas": [
    { "id": 1, "numero": "4750123456", "tipo": "AHORRO", "saldo": 1500000.00, "clienteId": 1 },
    { "id": 2, "numero": "4750654321", "tipo": "CORRIENTE", "saldo": 320000.50, "clienteId": 1 }
  ]
}
```

Confirmando que la llamada cruzada funciona:

```bash
docker compose exec cliente-service curl -s http://cuenta-service:8082/actuator/health
docker compose exec cuenta-service  curl -s http://cliente-service:8081/actuator/health
```

---

## 7. Pruebas con Postman

La carpeta [`postman/`](postman/) contiene **una sola colección** con subcarpetas, de
modo que al importarlas el orden queda fijo y no se mezcla con tus demás colecciones:

```
banca local
├── 00 - Integración bidireccional        7 peticiones
├── 01 - cliente-service (puerto 8081)    9 peticiones
└── 02 - cuenta-service (puerto 8082)    10 peticiones
```

| Archivo | Contenido |
|---|---|
| `postman/banca-local.postman_collection.json` | Colección única: 26 peticiones con aserciones |
| `postman/banca-local.postman_environment.json` | Variables `baseUrlCliente`, `baseUrlCuenta` y los ids |

Todas las peticiones incluyen **aserciones** en su test script (código HTTP, campos de
la respuesta y guardado automático del id creado para encadenar `POST → PUT → DELETE`).
Las variables viven en la colección, así que funciona incluso sin importar el entorno.
Además, `PUT` y `DELETE` incluyen un **pre-request script** que crea el recurso si el id
está vacío o ya no existe (p. ej. tras reiniciar los contenedores), por lo que esas
peticiones funcionan en cualquier orden.

```bash
docker compose up -d          # levantar antes de importar
```

Luego en Postman: **Import → Files** → seleccionar los `.json` y elegir el entorno
**Banca - Local**. Detalles y orden de ejecución en [`postman/README.md`](postman/README.md).

---

## 8. Pruebas

```bash
cd cliente-service && mvn test     # 15 pruebas
cd cuenta-service  && mvn test     # 16 pruebas
```

La suite cubre tres niveles por servicio:

- **Servicio** (`@Test` + Mockito): casos de uso, CRUD y propagación de errores.
- **Controlador** (`@WebMvcTest` + `MockMvc`): rutas, códigos HTTP, validación y
  manejo de errores globales.
- **Cliente HTTP** (`MockRestServiceServer`): verifica el **contrato exacto** que cada
  servicio espera del otro, sin levantar el microservicio remoto.

---

## 9. Notas de implementación (Spring Boot 4)

Tres detalles que cambiaron respecto a Spring Boot 3 y son relevantes para esta entrega:

1. **`@WebMvcTest` requiere un starter propio.** Los tests por capa se
   modularizaron: hace falta `spring-boot-starter-webmvc-test` además de
   `spring-boot-starter-test`.
2. **Los timeouts del cliente HTTP** se configuran con
   `HttpClientSettings` (`org.springframework.boot.http.client`), no con
   `connectTimeout()`/`readTimeout()` sobre el builder.
3. **`@MockBean` fue eliminado**; se usa `@MockitoBean`
   (`org.springframework.test.context.bean.override.mockito.MockitoBean`).

Los timeouts están en 3 segundos (`servicio.*.timeout-ms`) y, si el servicio remoto
no responde o devuelve un 5xx, la API responde `503 Service Unavailable` con un
`ErrorDTO` en lugar de propagar el error crudo.
