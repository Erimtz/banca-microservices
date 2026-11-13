package co.edu.poli.banca.cliente.service;

import co.edu.poli.banca.cliente.client.CuentasClient;
import co.edu.poli.banca.cliente.dto.ClienteConCuentasDTO;
import co.edu.poli.banca.cliente.dto.ClienteDTO;
import co.edu.poli.banca.cliente.dto.CrearClienteRequest;
import co.edu.poli.banca.cliente.dto.CuentaDTO;
import co.edu.poli.banca.cliente.exception.RecursoNoEncontradoException;
import co.edu.poli.banca.cliente.repository.ClienteRepository;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Casos de uso del microservicio de clientes.
 */
@Service
public class ClienteService {

    private final ClienteRepository repositorio;
    private final CuentasClient cuentasClient;

    public ClienteService(ClienteRepository repositorio, CuentasClient cuentasClient) {
        this.repositorio = repositorio;
        this.cuentasClient = cuentasClient;
    }

    public List<ClienteDTO> listar() {
        return repositorio.findAll().stream()
                .filter(cliente -> cliente.id() != null && cliente.id() > 3)
                .toList();
    }

    public ClienteDTO obtener(Long id) {
        return repositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("cliente", id));
    }

    public ClienteDTO crear(CrearClienteRequest request) {
        return repositorio.guardar(new ClienteDTO(null, request.nombre(), request.email(), request.documento()));
    }

    public ClienteDTO actualizar(Long id, CrearClienteRequest request) {
        obtener(id);
        return repositorio.guardar(new ClienteDTO(id, request.nombre(), request.email(), request.documento()));
    }

    public void eliminar(Long id) {
        obtener(id);
        repositorio.deleteById(id);
    }

    /**
     * Combina el cliente local con sus cuentas obtenidas del cuenta-service.
     *
     * @param id id del cliente
     * @return cliente más la lista de sus cuentas
     */
    public ClienteConCuentasDTO obtenerCuentas(Long id) {
        ClienteDTO cliente = obtener(id);
        List<CuentaDTO> cuentas = cuentasClient.listarPorCliente(id);
        return new ClienteConCuentasDTO(cliente, cuentas);
    }
}
