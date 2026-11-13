package co.edu.poli.banca.cuenta.repository;

import co.edu.poli.banca.cuenta.dto.CuentaDTO;
import java.util.List;
import java.util.Optional;

/**
 * Puerto (interfaz) de persistencia de cuentas.
 *
 * <p>El servicio solo conoce esta interfaz; la implementación en producción es
 * {@link CuentaRepositoryJpa} (Spring Data JPA + H2 en modo archivo, respaldado
 * por el volumen {@code cuenta-data}) y en los tests unitarios
 * {@code CuentaRepositoryEnMemoria}.</p>
 */
public interface CuentaRepository {

    List<CuentaDTO> findAll();

    Optional<CuentaDTO> findById(Long id);

    List<CuentaDTO> findByClienteId(Long clienteId);

    /**
     * Guarda la cuenta. Si el id es {@code null} se asigna uno nuevo.
     *
     * @return la cuenta ya persistida, siempre con id asignado
     */
    CuentaDTO guardar(CuentaDTO cuenta);

    void deleteById(Long id);
}
