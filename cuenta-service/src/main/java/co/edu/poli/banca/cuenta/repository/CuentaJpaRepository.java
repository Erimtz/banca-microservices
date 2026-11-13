package co.edu.poli.banca.cuenta.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio Spring Data JPA sobre {@link CuentaEntity}.
 *
 * <p>El único método derivado es {@code findByClienteId}; el resto
 * ({@code save}, {@code findById}, {@code findAll}, {@code deleteById}...)
 * lo aporta {@link JpaRepository}.</p>
 */
public interface CuentaJpaRepository extends JpaRepository<CuentaEntity, Long> {

    List<CuentaEntity> findByClienteId(Long clienteId);
}
