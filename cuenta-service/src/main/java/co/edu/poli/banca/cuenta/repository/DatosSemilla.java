package co.edu.poli.banca.cuenta.repository;

import co.edu.poli.banca.cuenta.dto.CuentaDTO;
import java.math.BigDecimal;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Siembra las cuentas de ejemplo solo si la tabla está vacía.
 *
 * <p>Como H2 vive en un archivo dentro del volumen, en los arranques
 * siguientes ya hay datos y no se vuelve a sembrar (clave para que el
 * volumen realmente conserve lo que se guarda con {@code POST}).</p>
 */
@Component
public class DatosSemilla implements CommandLineRunner {

    private final CuentaRepository repositorio;

    public DatosSemilla(CuentaRepository repositorio) {
        this.repositorio = repositorio;
    }

    @Override
    public void run(String... args) {
        if (!repositorio.findAll().isEmpty()) {
            return;
        }
        repositorio.guardar(new CuentaDTO(null, "4750123456", "AHORRO", new BigDecimal("1500000.00"), 1L));
        repositorio.guardar(new CuentaDTO(null, "4750654321", "CORRIENTE", new BigDecimal("320000.50"), 1L));
        repositorio.guardar(new CuentaDTO(null, "4750112233", "AHORRO", new BigDecimal("890000.00"), 2L));
        repositorio.guardar(new CuentaDTO(null, "4750998877", "DEPOSITO", new BigDecimal("5400000.00"), 3L));
    }
}
