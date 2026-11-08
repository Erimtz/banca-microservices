package co.edu.poli.banca.cliente.dto;

import java.math.BigDecimal;

/**
 * Vista de una cuenta bancaria tal como la devuelve el {@code cuenta-service}.
 *
 * <p>Este DTO vive copiado en ambos microservicios: cada servicio define su
 * propio contrato de datos y no comparte clases entre sí.</p>
 *
 * @param id        identificador de la cuenta
 * @param numero    número de cuenta
 * @param tipo      tipo de cuenta (AHORRO, CORRIENTE, ...)
 * @param saldo     saldo disponible
 * @param clienteId identificador del cliente titular
 */
public record CuentaDTO(Long id, String numero, String tipo, BigDecimal saldo, Long clienteId) {
}
