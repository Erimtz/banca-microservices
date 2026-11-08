package co.edu.poli.banca.cliente.dto;

/**
 * Representación pública de un cliente tal como se expone por la API REST.
 *
 * @param id        identificador único del cliente
 * @param nombre    nombre completo
 * @param email     correo electrónico
 * @param documento número de documento de identidad
 */
public record ClienteDTO(Long id, String nombre, String email, String documento) {
}
