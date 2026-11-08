package co.edu.poli.banca.cuenta.exception;

/**
 * Se lanza cuando se solicita una cuenta que no existe.
 */
public class RecursoNoEncontradoException extends RuntimeException {

    private final String recurso;
    private final Long id;

    public RecursoNoEncontradoException(String recurso, Long id) {
        super("No existe " + recurso + " con id " + id);
        this.recurso = recurso;
        this.id = id;
    }

    public String getRecurso() {
        return recurso;
    }

    public Long getId() {
        return id;
    }
}
