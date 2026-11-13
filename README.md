# banca-microservices

Aplicación de banca de ejemplo compuesta por **dos microservicios** construidos con
**Java 21**, **Spring Boot 4.1.2** y **Docker**, que se comunican entre sí de forma
síncrona por REST dentro de una red de Docker.

- `cliente-service` (puerto **8081**): gestión de clientes bancarios.
- `cuenta-service` (puerto **8082**): gestión de cuentas bancarias (H2 con persistencia
  en volumen Docker).

---

## Arquitectura

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
                 │   ┌──────────────────┐    ┌───────────────┐  │
                 │   │  cuenta-service  │    │ volumen       │  │
                 │   │   puerto 8082    │───►│ cuenta-data   │  │
                 │   └──────────────────┘    └───────────────┘  │
                 └──────────────────────────────────────────────┘
```

La comunicación es **bidireccional**: cada microservicio expone un endpoint propio que
consume al otro **por el nombre del servicio dentro de la red de Docker**
(`http://cuenta-service:8082` y `http://cliente-service:8081`), no por `localhost`:

| Sentido | Endpoint propio | Endpoint consumido | Cliente HTTP |
|---|---|---|---|
| `cliente-service` → `cuenta-service` | `GET /api/clientes/{id}/cuentas` | `GET /api/cuentas/cliente/{clienteId}` | `CuentasClient` |
| `cuenta-service` → `cliente-service` | `GET /api/cuentas/{id}/titular` | `GET /api/clientes/{id}` | `ClientesClient` |

Cada servicio define sus propios DTOs (no comparten clases): es el comportamiento
esperado en un sistema desacoplado.

---

## Requisitos

- Docker Desktop con `docker compose` v2
- Java 21 + Maven 3.9 (solo si se quiere correr sin Docker)

---

## Cómo ejecutarlo

```bash
git clone https://github.com/Erimtz/banca-microservices.git
cd banca-microservices
docker compose up -d --build
docker compose ps        # los dos contenedores deben quedar "healthy"
```

Las variables de entorno son opcionales: `docker-compose.yml` trae valores por defecto
(`${VAR:-default}`). Para sobrescribirlos:

```bash
cp .env.example .env     # editar los valores deseados
docker compose up -d
```

Detener:

```bash
docker compose down          # conserva los datos de cuenta-service
docker compose down -v       # borra también el volumen de datos
```

### Sin Docker (desarrollo local)

```bash
# Terminal 1
cd cuenta-service && mvn spring-boot:run     # :8082
# Terminal 2
cd cliente-service && mvn spring-boot:run    # :8081
```

---

## Endpoints

### cliente-service — `http://localhost:8081`

| Método | Ruta | Descripción |
|---|---|---|
| `GET` | `/api/clientes` | Lista todos los clientes |
| `GET` | `/api/clientes/{id}` | Detalle de un cliente |
| `POST` | `/api/clientes` | Crea un cliente (validado) |
| `PUT` | `/api/clientes/{id}` | Actualiza un cliente (validado) |
| `DELETE` | `/api/clientes/{id}` | Elimina un cliente |
| `GET` | `/api/clientes/{id}/cuentas` | Cliente con sus cuentas (**consume al `cuenta-service`**) |
| `GET` | `/actuator/health` | Health check |

### cuenta-service — `http://localhost:8082`

| Método | Ruta | Descripción |
|---|---|---|
| `GET` | `/api/cuentas` | Lista todas las cuentas |
| `GET` | `/api/cuentas/{id}` | Detalle de una cuenta |
| `POST` | `/api/cuentas` | Crea una cuenta (validada) |
| `PUT` | `/api/cuentas/{id}` | Actualiza una cuenta (validada) |
| `DELETE` | `/api/cuentas/{id}` | Elimina una cuenta |
| `GET` | `/api/cuentas/cliente/{clienteId}` | Cuentas de un titular (lo consume `cliente-service`) |
| `GET` | `/api/cuentas/{id}/titular` | Cuenta con su titular (**consume al `cliente-service`**) |
| `GET` | `/actuator/health` | Health check |

---

## Ejemplo de uso (ida y vuelta)

```bash
# 1. Guardar un cliente
curl -s -X POST http://localhost:8081/api/clientes \
  -H "Content-Type: application/json" \
  -d '{"nombre":"Ana Gómez","email":"ana@ejemplo.com","documento":"1023456789"}'
# -> 201 {"id":4,...}

# 2. Guardar una cuenta de ese cliente
curl -s -X POST http://localhost:8082/api/cuentas \
  -H "Content-Type: application/json" \
  -d '{"numero":"4750123123","tipo":"AHORRO","saldo":987654.32,"clienteId":4}'
# -> 201 {"id":5,...}

# 3. Leer la cuenta con su titular (cuenta-service llama al cliente-service)
curl -s http://localhost:8082/api/cuentas/5/titular
# -> 200 {"cuenta":{...},"titular":{"nombre":"Ana Gómez",...}}

# 4. Leer el cliente con sus cuentas (cliente-service llama al cuenta-service)
curl -s http://localhost:8081/api/clientes/4/cuentas
# -> 200 {"cliente":{...},"cuentas":[{...}]}
```

---

## Datos

| Servicio | Persistencia |
|---|---|
| `cliente-service` | En memoria (`ConcurrentHashMap`), sembrado con 3 clientes en cada arranque |
| `cuenta-service` | **H2 en modo archivo** en `/app/data`, respaldado por el volumen `cuenta-data`: los datos sobreviven a reinicios; se siembra con 4 cuentas solo si la tabla está vacía |

Ambos servicios validan los payloads con Bean Validation y devuelven errores
consistentes (`400` con detalle por campo, `404`, `503` si el servicio remoto no
responde).

---

## Pruebas

```bash
cd cliente-service && mvn test     # 15 pruebas
cd cuenta-service  && mvn test     # 16 pruebas
```

La suite cubre casos de uso (Mockito), controladores (`@WebMvcTest` + `MockMvc`) y el
contrato HTTP que cada servicio espera del otro (`MockRestServiceServer`).

Para probar la API desde la interfaz: importar la colección de
[`postman/`](postman/README.md) en Postman (26 peticiones con aserciones) y elegir el
entorno **Banca - Local**.

---

## Estructura

```
repository/
├── docker-compose.yml          # Los dos contenedores, red y volumen cuenta-data
├── .env.example                # Variables de entorno documentadas
├── postman/                    # Colección Postman con aserciones
├── cliente-service/
│   ├── Dockerfile              # Build multi-stage: Maven → JRE
│   ├── pom.xml
│   └── src/main/java/...       # controller / service / repository / client / dto / exception
└── cuenta-service/
    ├── Dockerfile
    ├── pom.xml
    └── src/main/java/...       # incluye repository con JPA + H2
```

---

## Stack

| Componente | Versión |
|---|---|
| Java | 21 (Temurin LTS) |
| Spring Boot | 4.1.2 (Spring Framework 7) |
| Maven | 3.9.x |
| Persistencia (`cuenta-service`) | Spring Data JPA + H2 (modo archivo) |
| Docker / Docker Compose | 28.x / v2.34 |
| Imagen base runtime | `eclipse-temurin:21-jre` |
