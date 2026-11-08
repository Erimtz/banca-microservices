package co.edu.poli.banca.cliente.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Payload de entrada para crear o actualizar un cliente.
 */
public record CrearClienteRequest(
        @NotBlank(message = "el nombre es obligatorio")
        @Size(max = 100, message = "el nombre no puede superar los 100 caracteres")
        String nombre,

        @NotBlank(message = "el email es obligatorio")
        @Email(message = "el email no tiene un formato válido")
        @Size(max = 150, message = "el email no puede superar los 150 caracteres")
        String email,

        @NotBlank(message = "el documento es obligatorio")
        @Size(max = 20, message = "el documento no puede superar los 20 caracteres")
        String documento) {
}
