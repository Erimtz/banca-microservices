package co.edu.poli.banca.cuenta.controller;

import co.edu.poli.banca.cuenta.dto.CrearCuentaRequest;
import co.edu.poli.banca.cuenta.dto.CuentaDTO;
import co.edu.poli.banca.cuenta.service.CuentaService;
import co.edu.poli.banca.cuenta.service.CuentaService.CuentaConTitularDTO;
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
 * API REST del microservicio de cuentas bancarias.
 *
 * <ul>
 *   <li>{@code GET /api/cuentas/cliente/{clienteId}} es consumido por el cliente-service.</li>
 *   <li>{@code GET /api/cuentas/{id}/titular} consume al cliente-service.</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/cuentas")
public class CuentaController {

    private final CuentaService cuentaService;

    public CuentaController(CuentaService cuentaService) {
        this.cuentaService = cuentaService;
    }

    @GetMapping
    public List<CuentaDTO> listar() {
        return cuentaService.listar();
    }

    @GetMapping("/{id}")
    public CuentaDTO obtener(@PathVariable Long id) {
        return cuentaService.obtener(id);
    }

    /**
     * Endpoint de integración: lo consume el cliente-service para obtener
     * las cuentas de un cliente sin tener que exponer su repositorio.
     */
    @GetMapping("/cliente/{clienteId}")
    public List<CuentaDTO> listarPorCliente(@PathVariable Long clienteId) {
        return cuentaService.listarPorCliente(clienteId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<CuentaDTO> crear(@Valid @RequestBody CrearCuentaRequest request) {
        CuentaDTO creada = cuentaService.crear(request);
        return ResponseEntity
                .created(URI.create("/api/cuentas/" + creada.id()))
                .body(creada);
    }

    @PutMapping("/{id}")
    public CuentaDTO actualizar(@PathVariable Long id, @Valid @RequestBody CrearCuentaRequest request) {
        return cuentaService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        cuentaService.eliminar(id);
    }

    /**
     * Endpoint de integración: devuelve la cuenta junto con su titular,
     * obtenido consumiendo al cliente-service.
     */
    @GetMapping("/{id}/titular")
    public CuentaConTitularDTO titularDeLaCuenta(@PathVariable Long id) {
        return cuentaService.obtenerTitular(id);
    }
}
