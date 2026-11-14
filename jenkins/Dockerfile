# syntax=docker/dockerfile:1

# ============================================================================
# Jenkins LTS como contenedor, con las herramientas que el pipeline necesita:
#   - CLI de Docker + plugin Compose  (copiados de docker:cli, mismas versiones
#     que el motor de Docker Desktop: CLI 29 / Compose 5.7)
#   - git, curl y jq                  (checkout y aserciones en el pipeline)
#   - Plugins de Jenkins instalados como código (jenkins-plugin-cli):
#       Git, Pipeline, Docker Pipeline, Credentials, GitHub,
#       Pipeline Stage View + Pipeline REST API (vista de etapas)
# El volumen jenkins_home (definido en docker-compose.yml) conserva plugins,
# credenciales y jobs entre reinicios.
# ============================================================================

FROM jenkins/jenkins:lts

USER root

# --- Herramientas de SO ------------------------------------------------------
RUN apt-get update \
    && apt-get install -y --no-install-recommends git curl jq ca-certificates \
    && rm -rf /var/lib/apt/lists/*

# --- CLI de Docker + Compose + BuildKit (de la imagen oficial docker:cli) ----
# buildx es imprescindible: los Dockerfiles de los servicios usan
# RUN --mount=type=cache (sintaxis BuildKit) que el builder clasico rechaza.
COPY --from=docker:cli /usr/local/bin/docker /usr/local/bin/docker
COPY --from=docker:cli /usr/local/libexec/docker/cli-plugins/docker-compose \
                       /usr/local/lib/docker/cli-plugins/docker-compose
COPY --from=docker:cli /usr/local/libexec/docker/cli-plugins/docker-buildx \
                       /usr/local/lib/docker/cli-plugins/docker-buildx

# --- Maven 3.9 (misma imagen/version que el build stage de los servicios) ----
COPY --from=maven:3.9-eclipse-temurin-21 /usr/share/maven /usr/share/maven
RUN ln -s /usr/share/maven/bin/mvn /usr/local/bin/mvn

# --- Plugins de Jenkins (instalados como código, resuelve dependencias) -----
RUN jenkins-plugin-cli --plugins \
        git \
        github \
        workflow-aggregator \
        docker-workflow \
        credentials-binding \
        timestamper \
        junit \
        pipeline-stage-view \
        pipeline-rest-api

USER jenkins
