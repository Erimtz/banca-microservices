package co.edu.poli.banca.cliente.repository;

import co.edu.poli.banca.cliente.dto.ClienteDTO;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Component;

/**
 * Repositorio en memoria de clientes.
 *
 * <p>La primera entrega no incluye base de datos, por lo que los datos viven
 * en un {@link ConcurrentHashMap} y se siembra con información de ejemplo
 * al arrancar la aplicación.</p>
 */
@Component
public class ClienteRepository {

    private final Map<Long, ClienteDTO> datos = new ConcurrentHashMap<>();
    private final AtomicLong secuencia = new AtomicLong();

    public ClienteRepository() {
        guardar(new ClienteDTO(null, "Ana Gómez", "ana.gomez@ejemplo.com", "1023456789"));
        guardar(new ClienteDTO(null, "Luis Rodríguez", "luis.rodriguez@ejemplo.com", "80123456"));
        guardar(new ClienteDTO(null, "María Fernanda López", "maria.lopez@ejemplo.com", "1098765432"));
    }

    public List<ClienteDTO> findAll() {
        return datos.values().stream()
                .sorted(Comparator.comparing(ClienteDTO::id))
                .toList();
    }

    public Optional<ClienteDTO> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(datos.get(id));
    }

    public boolean existsById(Long id) {
        return id != null && datos.containsKey(id);
    }

    /**
     * Guarda el cliente. Si el id es {@code null} se asigna uno nuevo.
     *
     * @return el cliente ya persistido, siempre con id asignado
     */
    public ClienteDTO guardar(ClienteDTO cliente) {
        if (cliente.id() == null) {
            cliente = new ClienteDTO(
                    secuencia.incrementAndGet(),
                    cliente.nombre(),
                    cliente.email(),
                    cliente.documento());
        }
        datos.put(cliente.id(), cliente);
        return cliente;
    }

    public void deleteById(Long id) {
        if (id != null) {
            datos.remove(id);
        }
    }
}
