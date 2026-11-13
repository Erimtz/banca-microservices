# Postman — colección `banca local`

Carpeta con las pruebas de la primera entrega.

## Archivos

| Archivo | Contenido |
|---|---|
| `banca-local.postman_collection.json` | **Colección única** con 3 subcarpetas y 26 peticiones |
| `banca-local.postman_environment.json` | Variables de entorno (`baseUrlCliente`, `baseUrlCuenta`, ids de consulta) |
| `README.md` | Este archivo |

### Subcarpetas de la colección

```
banca local
├── 00 - Integración bidireccional        7 peticiones
├── 01 - cliente-service (puerto 8081)    9 peticiones
└── 02 - cuenta-service (puerto 8082)    10 peticiones
```

El orden va con prefijo numérico (`00`, `01`, `02`) para que se mantenga fijo dentro
de la colección.

> **Por qué una sola colección:** si importas colecciones sueltas, Postman las ordena
> alfabéticamente por nombre y el orden se pierde entre tus demás colecciones. Al
> importar **un único archivo**, todo queda anidado bajo `banca local` y el orden de
> las subcarpetas no depende del nombre del archivo.

## Cómo importar

1. Levanta los contenedores: `docker compose up -d`
2. En Postman: **Import → Files**
3. Selecciona los dos `.json` de esta carpeta
4. En la esquina superior derecha elige el entorno **Banca - Local**
   *(opcional: las 7 variables viven en la colección, así que funciona sin entorno;
   el entorno solo aporta las 5 variables de consulta para reutilizarlas en otras colecciones)*

> **Re-importa después de cada cambio:** si ya tenías la colección o el entorno importados,
> borra la versión vieja en Postman vuelve a importar los dos `.json`, para que apliquen
> los pre-request scripts y la variable `clienteIdCreado`/`cuentaIdCreada` del entorno
> eliminada.

## Variables

Las variables están declaradas **en la colección**, así que no dependes de importar el
entorno para que las peticiones funcionen.

| Variable | Valor por defecto | Descripción |
|---|---|---|
| `baseUrlCliente` | `http://localhost:8081` | URL pública del cliente-service |
| `baseUrlCuenta` | `http://localhost:8082` | URL pública del cuenta-service |
| `clienteId` | `1` | Cliente usado en las consultas |
| `cuentaId` | `1` | Cuenta usada en las consultas |
| `clienteIdTitular` | `1` | Titular usado en "cuentas por cliente" |
| `clienteIdCreado` | *(lo llena Postman)* | Id devuelto al crear un cliente |
| `cuentaIdCreada` | *(lo llena Postman)* | Id devuelto al crear una cuenta |

`clienteIdCreado` y `cuentaIdCreada` se guardan automáticamente desde el test script
del `POST`, por lo que `PUT` y `DELETE` funcionan sin editar nada a mano.

> **Nota sobre el entorno:** `clienteIdCreado` y `cuentaIdCreada` existen **solo en la
> colección**, no en el entorno. Si estuvieran en ambos, Postman prioriza el entorno
> (`environment > collection`) y una variable vacía del entorno eclipsaría el id
> guardado, haciendo que `PUT`/`DELETE` fallen con `404`.

### Auto-creación en `PUT` y `DELETE`

Las 4 peticiones dependientes (`Actualizar`/`Eliminar` cliente y cuenta) incluyen un
**pre-request script** que:

1. Si la variable del id está **vacía** → crea el recurso con un `POST`.
2. Si el id **ya no existe** (p. ej. tras reiniciar los contenedores) → lo crea de nuevo.
3. Guarda el id en ambos ámbitos antes de enviar la petición.

Así `PUT` y `DELETE` funcionan **en cualquier orden**, incluso sin ejecutar `Crear` antes
o después de un `docker compose restart`.

## Orden recomendado

Dentro de cada subcarpeta, ejecuta las peticiones de arriba hacia abajo:

1. `Health check`
2. Consultas (`GET`)
3. `Crear` → guarda el id
4. `Actualizar` / `Eliminar` → usan ese id
5. `Validación fallida` e `inexistente` → prueban los errores

> **Ojo:** `cliente-service` guarda en memoria, así que sus `DELETE` se pierden al
> reiniciar el contenedor. `cuenta-service` **sí persiste** en el volumen
> `cuenta-data`, por lo que sus datos sobreviven al reinicio. Para volver al estado
> inicial de ambos: `docker compose down -v && docker compose up -d --build`
> (⚠️ `down -v` borra también el volumen de cuentas).

## Ejecutar todo

**Runner → Selecciona `banca local` → Select All → Run.**

Son 26 peticiones, todas con aserciones; deben quedar en verde. Para ver las llamadas
cruzadas en vivo:

```bash
docker compose logs -f cliente-service cuenta-service
```

Cada petición de integración deja una línea como esta:

```
cliente-service | CuentasClient : [cliente-service] GET /api/cuentas/cliente/1 -> 2 cuenta(s)
cuenta-service  | ClientesClient: [cuenta-service]  GET /api/clientes/1 -> Ana Gómez
```
