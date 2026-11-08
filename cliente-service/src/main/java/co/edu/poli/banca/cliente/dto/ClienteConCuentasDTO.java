package co.edu.poli.banca.cliente.dto;

import java.util.List;

/**
 * Respuesta compuesta de {@code GET /api/clientes/{id}/cuentas}.
 *
 * <p>Combina el cliente (dato local) con sus cuentas (dato obtenido
 * consumiendo al {@code cuenta-service}), lo que hace visible la
 * comunicación entre ambos microservicios.</p>
 *
 * @param cliente cliente solicitado
 * @param cuentas cuentas devueltas por el cuenta-service
 */
public record ClienteConCuentasDTO(ClienteDTO cliente, List<CuentaDTO> cuentas) {
}
