# banca-microservices

AplicaciÃ³n de banca de ejemplo compuesta por **dos microservicios** construidos con
**Java 21**, **Spring Boot 4.1.2** y **Docker**, que se comunican entre sÃ­ de forma
sÃ­ncrona por REST dentro de una red de Docker.

- `cliente-service` (puerto **8081**): gestiÃ³n de clientes bancarios.
- `cuenta-service` (puerto **8082**): gestiÃ³n de cuentas bancarias (H2 con persistencia
  en volumen Docker).

---

## Arquitectura

```
                 â”Œâ”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”
                 â”‚            Red bridge: banca                 â”‚
                 â”‚                                              â”‚
   Cliente HTTP  â”‚   â”Œâ”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”    HTTP (RestClient)  â”‚
  â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â–ºâ”‚   â”‚ cliente-service  â”‚ â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â–º â”‚
   :8081         â”‚   â”‚   puerto 8081    â”‚   GET /api/cuentas/   â”‚
                 â”‚   â”‚                  â”‚   cliente/{id}        â”‚
                 â”‚   â””â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”˜                       â”‚
                 â”‚            â–²                                 â”‚
                 â”‚            â”‚ HTTP (RestClient)               â”‚
                 â”‚            â”‚ GET /api/clientes/{id}          â”‚
                 â”‚            â–¼                                 â”‚
                 â”‚   â”Œâ”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”    â”Œâ”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”  â”‚
                 â”‚   â”‚  cuenta-service  â”‚    â”‚ volumen       â”‚  â”‚
                 â”‚   â”‚   puerto 8082    â”‚â”€â”€â”€â–ºâ”‚ cuenta-data   â”‚  â”‚
                 â”‚   â””â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”˜    â””â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”˜  â”‚
                 â””â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”˜
```

La comunicaciÃ³n es **bidireccional**: cada microservicio expone un endpoint propio que
consume al otro **por el nombre del servicio dentro de la red de Docker**
(`http://cuenta-service:8082` y `http://cliente-service:8081`), no por `localhost`:

| Sentido | Endpoint propio | Endpoint consumido | Cliente HTTP |
|---|---|---|---|
| `cliente-service` â†’ `cuenta-service` | `GET /api/clientes/{id}/cuentas` | `GET /api/cuentas/cliente/{clienteId}` | `CuentasClient` |
| `cuenta-service` â†’ `cliente-service` | `GET /api/cuentas/{id}/titular` | `GET /api/clientes/{id}` | `ClientesClient` |

Cada servicio define sus propios DTOs (no comparten clases): es el comportamiento
esperado en un sistema desacoplado.

---

## Requisitos

- Docker Desktop con `docker compose` v2
- Java 21 + Maven 3.9 (solo si se quiere correr sin Docker)

---

## CÃ³mo ejecutarlo

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
docker compose down -v       # borra tambiÃ©n el volumen de datos
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

### cliente-service â€” `http://localhost:8081`

| MÃ©todo | Ruta | DescripciÃ³n |
|---|---|---|
| `GET` | `/api/clientes` | Lista todos los clientes |
| `GET` | `/api/clientes/{id}` | Detalle de un cliente |
| `POST` | `/api/clientes` | Crea un cliente (validado) |
| `PUT` | `/api/clientes/{id}` | Actualiza un cliente (validado) |
| `DELETE` | `/api/clientes/{id}` | Elimina un cliente |
| `GET` | `/api/clientes/{id}/cuentas` | Cliente con sus cuentas (**consume al `cuenta-service`**) |
| `GET` | `/actuator/health` | Health check |

### cuenta-service â€” `http://localhost:8082`

| MÃ©todo | Ruta | DescripciÃ³n |
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
  -d '{"nombre":"Ana GÃ³mez","email":"ana@ejemplo.com","documento":"1023456789"}'
# -> 201 {"id":4,...}

# 2. Guardar una cuenta de ese cliente
curl -s -X POST http://localhost:8082/api/cuentas \
  -H "Content-Type: application/json" \
  -d '{"numero":"4750123123","tipo":"AHORRO","saldo":987654.32,"clienteId":4}'
# -> 201 {"id":5,...}

# 3. Leer la cuenta con su titular (cuenta-service llama al cliente-service)
curl -s http://localhost:8082/api/cuentas/5/titular
# -> 200 {"cuenta":{...},"titular":{"nombre":"Ana GÃ³mez",...}}

# 4. Leer el cliente con sus cuentas (cliente-service llama al cuenta-service)
curl -s http://localhost:8081/api/clientes/4/cuentas
# -> 200 {"cliente":{...},"cuentas":[{...}]}
```

---

## Datos

| Servicio | Persistencia |
|---|---|
| `cliente-service` | En memoria (`ConcurrentHashMap`), sembrado con 3 clientes en cada arranque |
| `cuenta-service` | **H2 en modo archivo** en `/app/data`, respaldado por el volumen `cuenta-data`: los datos sobreviven a reinicios; se siembra con 4 cuentas solo si la tabla estÃ¡ vacÃ­a |

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

Para probar la API desde la interfaz: importar la colecciÃ³n de
[`postman/`](postman/README.md) en Postman (26 peticiones con aserciones) y elegir el
entorno **Banca - Local**.

---

## Estructura

```
repository/
â”œâ”€â”€ docker-compose.yml          # Los dos contenedores, red y volumen cuenta-data
â”œâ”€â”€ .env.example                # Variables de entorno documentadas
â”œâ”€â”€ postman/                    # ColecciÃ³n Postman con aserciones
â”œâ”€â”€ cliente-service/
â”‚   â”œâ”€â”€ Dockerfile              # Build multi-stage: Maven â†’ JRE
â”‚   â”œâ”€â”€ pom.xml
â”‚   â””â”€â”€ src/main/java/...       # controller / service / repository / client / dto / exception
â””â”€â”€ cuenta-service/
    â”œâ”€â”€ Dockerfile
    â”œâ”€â”€ pom.xml
    â””â”€â”€ src/main/java/...       # incluye repository con JPA + H2
```

---

## Stack

| Componente | VersiÃ³n |
|---|---|
| Java | 21 (Temurin LTS) |
| Spring Boot | 4.1.2 (Spring Framework 7) |
| Maven | 3.9.x |
| Persistencia (`cuenta-service`) | Spring Data JPA + H2 (modo archivo) |
| Docker / Docker Compose | 28.x / v2.34 |
| Imagen base runtime | `eclipse-temurin:21-jre` |

Este repositorio es consumido por el pipeline de Jenkins (Entrega 2) mediante el credential store de Jenkins.
