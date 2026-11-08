package co.edu.poli.banca.cuenta.service;

import co.edu.poli.banca.cuenta.client.ClientesClient;
import co.edu.poli.banca.cuenta.dto.CrearCuentaRequest;
import co.edu.poli.banca.cuenta.dto.CuentaDTO;
import co.edu.poli.banca.cuenta.dto.TitularDTO;
import co.edu.poli.banca.cuenta.exception.RecursoNoEncontradoException;
import co.edu.poli.banca.cuenta.repository.CuentaRepository;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Casos de uso del microservicio de cuentas bancarias.
 */
@Service
public class CuentaService {

    private final CuentaRepository repositorio;
    private final ClientesClient clientesClient;

    public CuentaService(CuentaRepository repositorio, ClientesClient clientesClient) {
        this.repositorio = repositorio;
        this.clientesClient = clientesClient;
    }

    public List<CuentaDTO> listar() {
        return repositorio.findAll();
    }

    public CuentaDTO obtener(Long id) {
        return repositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("cuenta", id));
    }

    public List<CuentaDTO> listarPorCliente(Long clienteId) {
        return repositorio.findByClienteId(clienteId);
    }

    public CuentaDTO crear(CrearCuentaRequest request) {
        return repositorio.guardar(new CuentaDTO(
                null, request.numero(), request.tipo(), request.saldo(), request.clienteId()));
    }

    public CuentaDTO actualizar(Long id, CrearCuentaRequest request) {
        obtener(id);
        return repositorio.guardar(new CuentaDTO(
                id, request.numero(), request.tipo(), request.saldo(), request.clienteId()));
    }

    public void eliminar(Long id) {
        obtener(id);
        repositorio.deleteById(id);
    }

    /**
     * Devuelve el titular de la cuenta consumiendo al cliente-service.
     *
     * @param id id de la cuenta
     * @return la cuenta junto con su titular
     */
    public CuentaConTitularDTO obtenerTitular(Long id) {
        CuentaDTO cuenta = obtener(id);
        TitularDTO titular = clientesClient.obtenerTitular(cuenta.clienteId());
        return new CuentaConTitularDTO(cuenta, titular);
    }

    /**
     * Respuesta compuesta de {@code GET /api/cuentas/{id}/titular}.
     *
     * @param cuenta cuenta solicitada
     * @param titular titular devuelto por el cliente-service
     */
    public record CuentaConTitularDTO(CuentaDTO cuenta, TitularDTO titular) {
    }
}
