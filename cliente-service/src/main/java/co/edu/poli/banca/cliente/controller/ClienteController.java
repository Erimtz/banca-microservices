package co.edu.poli.banca.cliente.controller;

import co.edu.poli.banca.cliente.dto.ClienteConCuentasDTO;
import co.edu.poli.banca.cliente.dto.ClienteDTO;
import co.edu.poli.banca.cliente.dto.CrearClienteRequest;
import co.edu.poli.banca.cliente.service.ClienteService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * API REST del microservicio de clientes.
 *
 * <p>{@code GET /api/clientes/{id}/cuentas} es el endpoint que consume al
 * {@code cuenta-service} mediante {@link co.edu.poli.banca.cliente.client.CuentasClient}.</p>
 */
@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @GetMapping
    public List<ClienteDTO> listar() {
        return clienteService.listar();
    }

    @GetMapping("/{id}")
    public ClienteDTO obtener(@PathVariable Long id) {
        return clienteService.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<ClienteDTO> crear(@Valid @RequestBody CrearClienteRequest request) {
        ClienteDTO creado = clienteService.crear(request);
        return ResponseEntity
                .created(URI.create("/api/clientes/" + creado.id()))
                .body(creado);
    }

    @PutMapping("/{id}")
    public ClienteDTO actualizar(@PathVariable Long id, @Valid @RequestBody CrearClienteRequest request) {
        return clienteService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        clienteService.eliminar(id);
    }

    /**
     * Endpoint de integración: devuelve el cliente junto con sus cuentas,
     * obtenidas consumiendo al cuenta-service.
     */
    @GetMapping("/{id}/cuentas")
    public ClienteConCuentasDTO cuentasDelCliente(@PathVariable Long id) {
        return clienteService.obtenerCuentas(id);
    }
}
