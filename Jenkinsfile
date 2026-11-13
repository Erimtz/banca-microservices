// ============================================================================
// Pipeline de la Entrega 2 — Jenkins toma el código de GitHub, construye las
// imágenes de los dos microservicios de la Entrega 1, corre los tests y
// verifica que los dos contenedores se comunican por el NOMBRE del servicio.
// Si algo falla, el build falla (bloque post con junit + logs + limpieza).
// ============================================================================

pipeline {
    agent any

    options {
        timestamps()                 // hora en cada línea de la consola
        disableConcurrentBuilds()    // un build a la vez sobre la misma pila
        timeout(time: 40, unit: 'MINUTES')
    }

    stages {
        stage('Checkout') {
            steps {
                // Toma el código del repositorio (la credencial vive en
                // Jenkins, nunca en este archivo)
                checkout scm
                sh 'git log -1 --oneline'
                sh 'git status --short'
            }
        }

        stage('Build') {
            steps {
                // Construye las imágenes de los dos contenedores de la Entrega 1
                sh 'docker compose build'
                sh 'docker images --format "{{.Repository}}:{{.Tag}} | {{.Size}}" | grep banca/ || true'
            }
        }

        stage('Test') {
            stages {
                stage('Test unitario - cliente-service') {
                    steps {
                        sh 'mvn -B -ntp -f cliente-service -Dmaven.repo.local=$JENKINS_HOME/.m2/repository test'
                    }
                }
                stage('Test unitario - cuenta-service') {
                    steps {
                        sh 'mvn -B -ntp -f cuenta-service -Dmaven.repo.local=$JENKINS_HOME/.m2/repository test'
                    }
                }
            }
        }

        stage('Verificacion (ida y vuelta)') {
            steps {
                sh '''
set -eu

echo "=== 1) Desplegar los dos contenedores con las imagenes del stage Build ==="
docker compose up -d --no-build

echo "=== 2) Esperar a que ambos healthchecks queden healthy ==="
h1=""; h2=""
for i in $(seq 1 40); do
    h1=$(docker inspect -f '{{.State.Health.Status}}' cliente-service 2>/dev/null || true)
    h2=$(docker inspect -f '{{.State.Health.Status}}' cuenta-service 2>/dev/null || true)
    if [ "$h1" = "healthy" ] && [ "$h2" = "healthy" ]; then
        echo "Ambos healthy (intento $i)"
        break
    fi
    sleep 3
done
docker compose ps
if [ "$h1" != "healthy" ] || [ "$h2" != "healthy" ]; then
    echo "ERROR: contenedores no quedaron healthy ($h1 / $h2)"
    docker compose logs --tail 40
    exit 1
fi

echo "=== 3) GUARDAR un cliente en el contenedor 1 ==="
CLIENTE=$(docker compose exec -T cliente-service curl -fsS -X POST http://localhost:8081/api/clientes \
    -H "Content-Type: application/json" \
    -d '{"nombre":"Jenkins CI","email":"jenkins@ejemplo.com","documento":"44444444"}')
echo "POST /api/clientes -> $CLIENTE"
CID=$(echo "$CLIENTE" | jq -r '.id')
if ! echo "$CID" | grep -Eq '^[0-9]+$'; then echo "ERROR: id de cliente invalido: $CID"; exit 1; fi

echo "=== 4) GUARDAR una cuenta en el contenedor 2 (referencia ese cliente) ==="
CUENTA=$(docker compose exec -T cuenta-service curl -fsS -X POST http://localhost:8082/api/cuentas \
    -H "Content-Type: application/json" \
    -d "{\\"numero\\":\\"4750000042\\",\\"tipo\\":\\"AHORRO\\",\\"saldo\\":123456,\\"clienteId\\":$CID}")
echo "POST /api/cuentas -> $CUENTA"
AID=$(echo "$CUENTA" | jq -r '.id')
if ! echo "$AID" | grep -Eq '^[0-9]+$'; then echo "ERROR: id de cuenta invalido: $AID"; exit 1; fi

echo "=== 5) LEER ida y vuelta por el NOMBRE del servicio ==="
echo "--- cuenta-service -> cliente-service (http://cliente-service:8081) ---"
TITULAR=$(docker compose exec -T cuenta-service curl -fsS "http://cliente-service:8081/api/clientes/$CID")
echo "$TITULAR"
echo "$TITULAR" | jq -e ".id == $CID" >/dev/null

echo "--- cliente-service -> cuenta-service (http://cuenta-service:8082) ---"
CUENTAS=$(docker compose exec -T cliente-service curl -fsS "http://cuenta-service:8082/api/cuentas/cliente/$CID")
echo "$CUENTAS"
echo "$CUENTAS" | jq -e "length >= 1" >/dev/null

echo "=== 6) Las llamadas HTTP cruzadas quedaron en los logs ==="
docker compose logs cliente-service cuenta-service | grep -E "CuentasClient|ClientesClient" | tail -4 || true

echo "=== 7) El volumen conserva el dato tras reiniciar cuenta-service ==="
docker compose restart cuenta-service
h=""
for i in $(seq 1 30); do
    h=$(docker inspect -f '{{.State.Health.Status}}' cuenta-service 2>/dev/null || true)
    if [ "$h" = "healthy" ]; then echo "cuenta-service healthy de nuevo (intento $i)"; break; fi
    sleep 3
done
if [ "$h" != "healthy" ]; then echo "ERROR: cuenta-service no volvio a healthy"; exit 1; fi
CUENTA2=$(docker compose exec -T cuenta-service curl -fsS "http://localhost:8082/api/cuentas/$AID")
echo "GET /api/cuentas/$AID tras restart -> $CUENTA2"
echo "$CUENTA2" | jq -e ".numero == \\"4750000042\\"" >/dev/null

echo "=== 8) Limpiar los datos creados por esta ejecucion ==="
code=$(docker compose exec -T cuenta-service curl -sS -o /dev/null -w '%{http_code}' -X DELETE "http://localhost:8082/api/cuentas/$AID")
echo "DELETE /api/cuentas/$AID -> $code"
if [ "$code" != "204" ]; then echo "ERROR: DELETE cuenta != 204"; exit 1; fi
code=$(docker compose exec -T cliente-service curl -sS -o /dev/null -w '%{http_code}' -X DELETE "http://localhost:8081/api/clientes/$CID")
echo "DELETE /api/clientes/$CID -> $code"
if [ "$code" != "204" ]; then echo "ERROR: DELETE cliente != 204"; exit 1; fi

echo "OK: ida y vuelta verificada entre los dos contenedores"
'''
            }
        }
    }

    post {
        always {
            // Los resultados de JUnit quedan visibles en el job (tendencia)
            junit allowEmptyResults: true, testResults: '**/target/surefire-reports/*.xml'
        }
        success {
            // Jenkins deja los dos contenedores desplegados y healthy
            sh 'docker compose ps || true'
        }
        failure {
            // Si algo falla el build queda en rojo y se conserva evidencia
            sh 'docker compose ps || true'
            sh 'docker compose logs --tail 80 || true'
            sh 'docker compose down -v || true'
        }
    }
}
