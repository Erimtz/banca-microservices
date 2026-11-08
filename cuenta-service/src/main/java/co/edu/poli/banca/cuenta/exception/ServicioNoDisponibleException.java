package co.edu.poli.banca.cuenta.exception;

/**
 * Se lanza cuando el microservicio remoto no responde o devuelve un error.
 */
public class ServicioNoDisponibleException extends RuntimeException {

    private final String servicio;

    public ServicioNoDisponibleException(String servicio, String mensaje, Throwable causa) {
        super(mensaje, causa);
        this.servicio = servicio;
    }

    public String getServicio() {
        return servicio;
    }
}
