package co.edu.poli.banca.cuenta.dto;

import java.math.BigDecimal;

/**
 * Representación pública de una cuenta bancaria.
 *
 * @param id        identificador único de la cuenta
 * @param numero    número de cuenta
 * @param tipo      tipo de cuenta (AHORRO, CORRIENTE, ...)
 * @param saldo     saldo disponible
 * @param clienteId identificador del cliente titular
 */
public record CuentaDTO(Long id, String numero, String tipo, BigDecimal saldo, Long clienteId) {
}
