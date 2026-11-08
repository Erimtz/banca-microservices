package co.edu.poli.banca.cuenta.exception;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Cuerpo estándar de error devuelto por el microservicio.
 */
public record ErrorDTO(String mensaje, String servicio, LocalDateTime timestamp, Map<String, String> detalle) {

    public static ErrorDTO simple(String mensaje, String servicio) {
        return new ErrorDTO(mensaje, servicio, LocalDateTime.now(), Map.of());
    }

    public static ErrorDTO conDetalle(String mensaje, String servicio, Map<String, String> detalle) {
        return new ErrorDTO(mensaje, servicio, LocalDateTime.now(), new LinkedHashMap<>(detalle));
    }
}
