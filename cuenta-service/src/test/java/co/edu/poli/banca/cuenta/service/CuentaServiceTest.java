package co.edu.poli.banca.cuenta.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import co.edu.poli.banca.cuenta.client.ClientesClient;
import co.edu.poli.banca.cuenta.dto.CrearCuentaRequest;
import co.edu.poli.banca.cuenta.dto.CuentaDTO;
import co.edu.poli.banca.cuenta.dto.TitularDTO;
import co.edu.poli.banca.cuenta.exception.RecursoNoEncontradoException;
import co.edu.poli.banca.cuenta.exception.ServicioNoDisponibleException;
import co.edu.poli.banca.cuenta.repository.CuentaRepository;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CuentaServiceTest {

    private CuentaRepository repositorio;
    private ClientesClient clientesClient;
    private CuentaService servicio;

    @BeforeEach
    void setUp() {
        repositorio = new CuentaRepository();
        clientesClient = mock(ClientesClient.class);
        servicio = new CuentaService(repositorio, clientesClient);
    }

    @Test
    void debeListarLasCuentasSembradas() {
        assertThat(servicio.listar()).hasSize(4);
    }

    @Test
    void debeDevolverLaCuentaPorId() {
        CuentaDTO cuenta = servicio.obtener(1L);

        assertThat(cuenta.numero()).isEqualTo("4750123456");
    }

    @Test
    void debeLanzar404CuandoLaCuentaNoExiste() {
        assertThatThrownBy(() -> servicio.obtener(999L))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessageContaining("999");
    }

    @Test
    void debeListarSoloLasCuentasDelCliente() {
        List<CuentaDTO> delCliente1 = servicio.listarPorCliente(1L);

        assertThat(delCliente1).hasSize(2);
        assertThat(delCliente1).allMatch(c -> c.clienteId().equals(1L));
    }

    @Test
    void debeCrearUnaCuentaConIdAsignado() {
        CrearCuentaRequest request =
                new CrearCuentaRequest("4750777777", "AHORRO", new BigDecimal("100.00"), 2L);

        CuentaDTO creada = servicio.crear(request);

        assertThat(creada.id()).isNotNull();
        assertThat(servicio.listar()).hasSize(5);
    }

    @Test
    void debeActualizarUnaCuentaExistente() {
        CrearCuentaRequest request =
                new CrearCuentaRequest("4750123456", "CORRIENTE", new BigDecimal("999.00"), 1L);

        CuentaDTO actualizada = servicio.actualizar(1L, request);

        assertThat(actualizada.tipo()).isEqualTo("CORRIENTE");
        assertThat(servicio.listar()).hasSize(4);
    }

    @Test
    void debeDevolverLaCuentaConSuTitularDesdeElClienteService() {
        TitularDTO titular = new TitularDTO(1L, "Ana Gómez", "ana.gomez@ejemplo.com", "1023456789");
        when(clientesClient.obtenerTitular(1L)).thenReturn(titular);

        CuentaService.CuentaConTitularDTO resultado = servicio.obtenerTitular(1L);

        assertThat(resultado.cuenta().id()).isEqualTo(1L);
        assertThat(resultado.titular().nombre()).isEqualTo("Ana Gómez");
        verify(clientesClient).obtenerTitular(1L);
    }

    @Test
    void debePropagarElErrorCuandoElClienteServiceNoResponde() {
        when(clientesClient.obtenerTitular(anyLong()))
                .thenThrow(new ServicioNoDisponibleException("cliente-service", "caído", null));

        assertThatThrownBy(() -> servicio.obtenerTitular(1L))
                .isInstanceOf(ServicioNoDisponibleException.class);
    }
}
