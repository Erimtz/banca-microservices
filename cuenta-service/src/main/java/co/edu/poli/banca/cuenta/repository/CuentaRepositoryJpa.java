package co.edu.poli.banca.cuenta.repository;

import co.edu.poli.banca.cuenta.dto.CuentaDTO;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

/**
 * Implementación del puerto {@link CuentaRepository} sobre Spring Data JPA.
 *
 * <p>Es la que usa el microservicio en producción: H2 en modo archivo dentro
 * del contenedor ({@code /app/data}), respaldado por el volumen
 * {@code cuenta-data} definido en docker-compose.yml. Traduce entre
 * {@link CuentaEntity} (persistencia) y {@link CuentaDTO} (API pública), de
 * modo que el resto de la aplicación no depende de JPA.</p>
 */
@Component
public class CuentaRepositoryJpa implements CuentaRepository {

    private final CuentaJpaRepository jpa;

    public CuentaRepositoryJpa(CuentaJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public List<CuentaDTO> findAll() {
        return jpa.findAll(Sort.by("id")).stream()
                .map(CuentaRepositoryJpa::aDTO)
                .toList();
    }

    @Override
    public Optional<CuentaDTO> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return jpa.findById(id).map(CuentaRepositoryJpa::aDTO);
    }

    @Override
    public List<CuentaDTO> findByClienteId(Long clienteId) {
        return jpa.findByClienteId(clienteId).stream()
                .sorted(Comparator.comparing(CuentaEntity::getId))
                .map(CuentaRepositoryJpa::aDTO)
                .toList();
    }

    @Override
    public CuentaDTO guardar(CuentaDTO cuenta) {
        return aDTO(jpa.save(aEntidad(cuenta)));
    }

    @Override
    public void deleteById(Long id) {
        if (id != null) {
            jpa.deleteById(id);
        }
    }

    private static CuentaDTO aDTO(CuentaEntity entidad) {
        return new CuentaDTO(
                entidad.getId(),
                entidad.getNumero(),
                entidad.getTipo(),
                entidad.getSaldo(),
                entidad.getClienteId());
    }

    private static CuentaEntity aEntidad(CuentaDTO dto) {
        return new CuentaEntity(
                dto.id(), dto.numero(), dto.tipo(), dto.saldo(), dto.clienteId());
    }
}
