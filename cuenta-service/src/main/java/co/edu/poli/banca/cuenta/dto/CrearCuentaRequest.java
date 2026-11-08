package co.edu.poli.banca.cuenta.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * Payload de entrada para crear o actualizar una cuenta.
 */
public record CrearCuentaRequest(
        @NotBlank(message = "el número de cuenta es obligatorio")
        @Size(max = 30, message = "el número de cuenta no puede superar los 30 caracteres")
        String numero,

        @NotBlank(message = "el tipo de cuenta es obligatorio")
        @Pattern(regexp = "AHORRO|CORRIENTE|DEPOSITO",
                message = "el tipo debe ser AHORRO, CORRIENTE o DEPOSITO")
        String tipo,

        @NotNull(message = "el saldo es obligatorio")
        @DecimalMin(value = "0.0", message = "el saldo no puede ser negativo")
        BigDecimal saldo,

        @NotNull(message = "el id del cliente titular es obligatorio")
        Long clienteId) {
}
