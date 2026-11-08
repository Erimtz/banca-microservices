# syntax=docker/dockerfile:1

# ---------------------------------------------------------------------------
# Etapa 1: construye el JAR con Maven sobre Java 21
# ---------------------------------------------------------------------------
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /build

# El .dockerignore excluye target/, así que solo entran pom.xml y src/
COPY . .

# El montaje de caché en /root/.m2 evita volver a descargar dependencias
# en cada reconstrucción del contenedor.
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -ntp -DskipTests package

# ---------------------------------------------------------------------------
# Etapa 2: imagen mínima de ejecución solo con el JRE
# ---------------------------------------------------------------------------
FROM eclipse-temurin:21-jre

# curl es necesario para el healthcheck definido en docker-compose.yml
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

RUN groupadd --system app && useradd --system --gid app --home-dir /app app

WORKDIR /app

COPY --from=build /build/target/*.jar app.jar

RUN chown -R app:app /app
USER app

EXPOSE 8081

HEALTHCHECK --interval=10s --timeout=5s --start-period=40s --retries=5 \
  CMD curl -fsS http://localhost:8081/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
