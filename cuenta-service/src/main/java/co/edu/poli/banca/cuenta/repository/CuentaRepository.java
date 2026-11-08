package co.edu.poli.banca.cuenta.repository;

import co.edu.poli.banca.cuenta.dto.CuentaDTO;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Component;

/**
 * Repositorio en memoria de cuentas bancarias.
 *
 * <p>La primera entrega no incluye base de datos, por lo que los datos viven
 * en un {@link ConcurrentHashMap} y se siembra con información de ejemplo
 * al arrancar la aplicación.</p>
 */
@Component
public class CuentaRepository {

    private final Map<Long, CuentaDTO> datos = new ConcurrentHashMap<>();
    private final AtomicLong secuencia = new AtomicLong();

    public CuentaRepository() {
        guardar(new CuentaDTO(null, "4750123456", "AHORRO", new BigDecimal("1500000.00"), 1L));
        guardar(new CuentaDTO(null, "4750654321", "CORRIENTE", new BigDecimal("320000.50"), 1L));
        guardar(new CuentaDTO(null, "4750112233", "AHORRO", new BigDecimal("890000.00"), 2L));
        guardar(new CuentaDTO(null, "4750998877", "DEPOSITO", new BigDecimal("5400000.00"), 3L));
    }

    public List<CuentaDTO> findAll() {
        return datos.values().stream()
                .sorted(Comparator.comparing(CuentaDTO::id))
                .toList();
    }

    public Optional<CuentaDTO> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(datos.get(id));
    }

    public List<CuentaDTO> findByClienteId(Long clienteId) {
        return datos.values().stream()
                .filter(cuenta -> cuenta.clienteId().equals(clienteId))
                .sorted(Comparator.comparing(CuentaDTO::id))
                .toList();
    }

    /**
     * Guarda la cuenta. Si el id es {@code null} se asigna uno nuevo.
     *
     * @return la cuenta ya persistida, siempre con id asignado
     */
    public CuentaDTO guardar(CuentaDTO cuenta) {
        if (cuenta.id() == null) {
            cuenta = new CuentaDTO(
                    secuencia.incrementAndGet(),
                    cuenta.numero(),
                    cuenta.tipo(),
                    cuenta.saldo(),
                    cuenta.clienteId());
        }
        datos.put(cuenta.id(), cuenta);
        return cuenta;
    }

    public void deleteById(Long id) {
        if (id != null) {
            datos.remove(id);
        }
    }
}
