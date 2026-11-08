package co.edu.poli.banca.cliente.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import co.edu.poli.banca.cliente.client.CuentasClient;
import co.edu.poli.banca.cliente.dto.ClienteConCuentasDTO;
import co.edu.poli.banca.cliente.dto.ClienteDTO;
import co.edu.poli.banca.cliente.dto.CrearClienteRequest;
import co.edu.poli.banca.cliente.dto.CuentaDTO;
import co.edu.poli.banca.cliente.exception.RecursoNoEncontradoException;
import co.edu.poli.banca.cliente.exception.ServicioNoDisponibleException;
import co.edu.poli.banca.cliente.repository.ClienteRepository;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ClienteServiceTest {

    private ClienteRepository repositorio;
    private CuentasClient cuentasClient;
    private ClienteService servicio;

    @BeforeEach
    void setUp() {
        repositorio = new ClienteRepository();
        cuentasClient = mock(CuentasClient.class);
        servicio = new ClienteService(repositorio, cuentasClient);
    }

    @Test
    void debeListarLosClientesSembrados() {
        assertThat(servicio.listar()).hasSize(3);
    }

    @Test
    void debeDevolverElClientePorId() {
        ClienteDTO cliente = servicio.obtener(1L);

        assertThat(cliente.nombre()).isEqualTo("Ana Gómez");
    }

    @Test
    void debeLanzar404CuandoElClienteNoExiste() {
        assertThatThrownBy(() -> servicio.obtener(999L))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessageContaining("999");
    }

    @Test
    void debeCrearUnClienteConIdAsignado() {
        CrearClienteRequest request =
                new CrearClienteRequest("Pedro", "pedro@ejemplo.com", "123456789");

        ClienteDTO creado = servicio.crear(request);

        assertThat(creado.id()).isNotNull();
        assertThat(servicio.listar()).hasSize(4);
    }

    @Test
    void debeActualizarUnClienteExistente() {
        CrearClienteRequest request =
                new CrearClienteRequest("Ana Actualizada", "ana@ejemplo.com", "1023456789");

        ClienteDTO actualizado = servicio.actualizar(1L, request);

        assertThat(actualizado.nombre()).isEqualTo("Ana Actualizada");
        assertThat(servicio.listar()).hasSize(3);
    }

    @Test
    void debeEliminarUnClienteExistente() {
        servicio.eliminar(1L);

        assertThat(servicio.listar()).hasSize(2);
        assertThatThrownBy(() -> servicio.obtener(1L))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void debeCombinarElClienteLocalConLasCuentasDelCuentaService() {
        List<CuentaDTO> cuentas = List.of(
                new CuentaDTO(1L, "4750123456", "AHORRO", new BigDecimal("1500000.00"), 1L));
        when(cuentasClient.listarPorCliente(1L)).thenReturn(cuentas);

        ClienteConCuentasDTO resultado = servicio.obtenerCuentas(1L);

        assertThat(resultado.cliente().id()).isEqualTo(1L);
        assertThat(resultado.cuentas()).hasSize(1);
        assertThat(resultado.cuentas().get(0).numero()).isEqualTo("4750123456");
        verify(cuentasClient).listarPorCliente(1L);
    }

    @Test
    void debePropagarElErrorCuandoElCuentaServiceNoResponde() {
        when(cuentasClient.listarPorCliente(anyLong()))
                .thenThrow(new ServicioNoDisponibleException("cuenta-service", "caído", null));

        assertThatThrownBy(() -> servicio.obtenerCuentas(1L))
                .isInstanceOf(ServicioNoDisponibleException.class);
    }
}
