package co.edu.poli.banca.cliente.client;

import co.edu.poli.banca.cliente.dto.CuentaDTO;
import co.edu.poli.banca.cliente.exception.RecursoNoEncontradoException;
import co.edu.poli.banca.cliente.exception.ServicioNoDisponibleException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

/**
 * Cliente HTTP hacia el microservicio {@code cuenta-service}.
 *
 * <p>Es la punta de la comunicación <strong>cliente-service → cuenta-service</strong>
 * de la relación bidireccional entre ambos contenedores.</p>
 */
@Component
public class CuentasClient {

    private static final Logger log = LoggerFactory.getLogger(CuentasClient.class);

    private static final ParameterizedTypeReference<List<CuentaDTO>> LISTA_CUENTAS =
            new ParameterizedTypeReference<>() {
            };

    private final RestClient restClient;
    private final String nombreServicio;

    public CuentasClient(
            @Qualifier("cuentasRestClient") RestClient restClient,
            @Value("${servicio.nombre:cliente-service}") String nombreServicio) {
        this.restClient = restClient;
        this.nombreServicio = nombreServicio;
    }

    /**
     * Consulta las cuentas que pertenecen a un cliente.
     *
     * @param clienteId id del cliente titular
     * @return lista de cuentas, vacía si no tiene ninguna
     * @throws ServicioNoDisponibleException si el cuenta-service no responde
     */
    public List<CuentaDTO> listarPorCliente(Long clienteId) {
        try {
            List<CuentaDTO> cuentas = restClient.get()
                    .uri("/api/cuentas/cliente/{id}", clienteId)
                    .retrieve()
                    .body(LISTA_CUENTAS);

            log.info("[{}] GET /api/cuentas/cliente/{} -> {} cuenta(s)",
                    nombreServicio, clienteId, cuentas == null ? 0 : cuentas.size());

            return cuentas == null ? List.of() : cuentas;

        } catch (ResourceAccessException ex) {
            log.error("[{}] No se pudo contactar al cuenta-service: {}", nombreServicio, ex.getMessage());
            throw new ServicioNoDisponibleException(
                    "cuenta-service",
                    "No fue posible contactar al cuenta-service",
                    ex);
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().value() == 404) {
                throw new RecursoNoEncontradoException("cliente", clienteId);
            }
            if (ex.getStatusCode().is5xxServerError()) {
                log.error("[{}] El cuenta-service devolvió {}: {}",
                        nombreServicio, ex.getStatusCode().value(), ex.getMessage());
                throw new ServicioNoDisponibleException(
                        "cuenta-service",
                        "El cuenta-service devolvió un error " + ex.getStatusCode().value(),
                        ex);
            }
            throw ex;
        }
    }
}
