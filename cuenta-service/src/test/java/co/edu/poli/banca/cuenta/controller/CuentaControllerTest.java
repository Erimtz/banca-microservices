package co.edu.poli.banca.cuenta.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import co.edu.poli.banca.cuenta.dto.CrearCuentaRequest;
import co.edu.poli.banca.cuenta.dto.CuentaDTO;
import co.edu.poli.banca.cuenta.dto.TitularDTO;
import co.edu.poli.banca.cuenta.exception.RecursoNoEncontradoException;
import co.edu.poli.banca.cuenta.service.CuentaService;
import co.edu.poli.banca.cuenta.service.CuentaService.CuentaConTitularDTO;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CuentaController.class)
class CuentaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CuentaService cuentaService;

    @Test
    void debeListarCuentas() throws Exception {
        when(cuentaService.listar()).thenReturn(List.of(
                new CuentaDTO(1L, "4750123456", "AHORRO", new BigDecimal("1500000.00"), 1L)));

        mockMvc.perform(get("/api/cuentas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].numero").value("4750123456"))
                .andExpect(jsonPath("$[0].clienteId").value(1));
    }

    @Test
    void debeDevolver404CuandoLaCuentaNoExiste() throws Exception {
        when(cuentaService.obtener(99L))
                .thenThrow(new RecursoNoEncontradoException("cuenta", 99L));

        mockMvc.perform(get("/api/cuentas/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("No existe cuenta con id 99"))
                .andExpect(jsonPath("$.servicio").value("cuenta-service"));
    }

    @Test
    void debeListarLasCuentasDeUnCliente() throws Exception {
        when(cuentaService.listarPorCliente(1L)).thenReturn(List.of(
                new CuentaDTO(1L, "4750123456", "AHORRO", new BigDecimal("1500000.00"), 1L),
                new CuentaDTO(2L, "4750654321", "CORRIENTE", new BigDecimal("320000.50"), 1L)));

        mockMvc.perform(get("/api/cuentas/cliente/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].clienteId").value(1));
    }

    @Test
    void debeValidarElPayloadDeCreacion() throws Exception {
        String cuerpo = """
                {"numero":"","tipo":"OTRO","saldo":-5,"clienteId":null}
                """;

        mockMvc.perform(post("/api/cuentas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("La petición no es válida"))
                .andExpect(jsonPath("$.detalle.numero").exists())
                .andExpect(jsonPath("$.detalle.tipo").exists())
                .andExpect(jsonPath("$.detalle.saldo").exists())
                .andExpect(jsonPath("$.detalle.clienteId").exists());
    }

    @Test
    void debeCrearUnaCuenta() throws Exception {
        when(cuentaService.crear(any(CrearCuentaRequest.class)))
                .thenReturn(new CuentaDTO(9L, "4750121212", "AHORRO", new BigDecimal("10.00"), 2L));

        String cuerpo = """
                {"numero":"4750121212","tipo":"AHORRO","saldo":10.00,"clienteId":2}
                """;

        mockMvc.perform(post("/api/cuentas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/cuentas/9"))
                .andExpect(jsonPath("$.id").value(9));
    }

    @Test
    void debeDevolverLaCuentaConSuTitular() throws Exception {
        CuentaDTO cuenta = new CuentaDTO(1L, "4750123456", "AHORRO", new BigDecimal("1500000.00"), 1L);
        TitularDTO titular = new TitularDTO(1L, "Ana Gómez", "ana.gomez@ejemplo.com", "1023456789");

        when(cuentaService.obtenerTitular(1L)).thenReturn(new CuentaConTitularDTO(cuenta, titular));

        mockMvc.perform(get("/api/cuentas/1/titular"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cuenta.numero").value("4750123456"))
                .andExpect(jsonPath("$.titular.nombre").value("Ana Gómez"))
                .andExpect(jsonPath("$.titular.email").value("ana.gomez@ejemplo.com"));
    }
}
