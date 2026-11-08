package co.edu.poli.banca.cuenta.client;

import co.edu.poli.banca.cuenta.dto.TitularDTO;
import co.edu.poli.banca.cuenta.exception.RecursoNoEncontradoException;
import co.edu.poli.banca.cuenta.exception.ServicioNoDisponibleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

/**
 * Cliente HTTP hacia el microservicio {@code cliente-service}.
 *
 * <p>Es la punta de la comunicación <strong>cuenta-service → cliente-service</strong>
 * de la relación bidireccional entre ambos contenedores.</p>
 */
@Component
public class ClientesClient {

    private static final Logger log = LoggerFactory.getLogger(ClientesClient.class);

    private final RestClient restClient;
    private final String nombreServicio;

    public ClientesClient(
            @Qualifier("clientesRestClient") RestClient restClient,
            @Value("${servicio.nombre:cuenta-service}") String nombreServicio) {
        this.restClient = restClient;
        this.nombreServicio = nombreServicio;
    }

    /**
     * Consulta el cliente titular de una cuenta.
     *
     * @param clienteId id del cliente titular
     * @return el cliente
     * @throws ServicioNoDisponibleException si el cliente-service no responde
     */
    public TitularDTO obtenerTitular(Long clienteId) {
        try {
            TitularDTO titular = restClient.get()
                    .uri("/api/clientes/{id}", clienteId)
                    .retrieve()
                    .body(TitularDTO.class);

            log.info("[{}] GET /api/clientes/{} -> {}", nombreServicio, clienteId,
                    titular == null ? "sin datos" : titular.nombre());

            return titular;

        } catch (ResourceAccessException ex) {
            log.error("[{}] No se pudo contactar al cliente-service: {}", nombreServicio, ex.getMessage());
            throw new ServicioNoDisponibleException(
                    "cliente-service",
                    "No fue posible contactar al cliente-service",
                    ex);
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().value() == 404) {
                throw new RecursoNoEncontradoException("cliente", clienteId);
            }
            if (ex.getStatusCode().is5xxServerError()) {
                log.error("[{}] El cliente-service devolvió {}: {}",
                        nombreServicio, ex.getStatusCode().value(), ex.getMessage());
                throw new ServicioNoDisponibleException(
                        "cliente-service",
                        "El cliente-service devolvió un error " + ex.getStatusCode().value(),
                        ex);
            }
            throw ex;
        }
    }
}
