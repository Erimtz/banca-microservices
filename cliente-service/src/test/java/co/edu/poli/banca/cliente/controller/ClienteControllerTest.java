package co.edu.poli.banca.cliente.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import co.edu.poli.banca.cliente.dto.ClienteConCuentasDTO;
import co.edu.poli.banca.cliente.dto.ClienteDTO;
import co.edu.poli.banca.cliente.dto.CrearClienteRequest;
import co.edu.poli.banca.cliente.dto.CuentaDTO;
import co.edu.poli.banca.cliente.exception.RecursoNoEncontradoException;
import co.edu.poli.banca.cliente.service.ClienteService;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ClienteController.class)
class ClienteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ClienteService clienteService;

    @Test
    void debeListarClientes() throws Exception {
        when(clienteService.listar()).thenReturn(List.of(
                new ClienteDTO(1L, "Ana Gómez", "ana.gomez@ejemplo.com", "1023456789")));

        mockMvc.perform(get("/api/clientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].nombre").value("Ana Gómez"));
    }

    @Test
    void debeDevolver404CuandoElClienteNoExiste() throws Exception {
        when(clienteService.obtener(99L))
                .thenThrow(new RecursoNoEncontradoException("cliente", 99L));

        mockMvc.perform(get("/api/clientes/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("No existe cliente con id 99"))
                .andExpect(jsonPath("$.servicio").value("cliente-service"));
    }

    @Test
    void debeValidarElPayloadDeCreacion() throws Exception {
        String cuerpo = """
                {"nombre":"","email":"no-es-un-email","documento":""}
                """;

        mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("La petición no es válida"))
                .andExpect(jsonPath("$.detalle.nombre").value("el nombre es obligatorio"))
                .andExpect(jsonPath("$.detalle.documento").value("el documento es obligatorio"));
    }

    @Test
    void debeCrearUnCliente() throws Exception {
        when(clienteService.crear(any(CrearClienteRequest.class)))
                .thenReturn(new ClienteDTO(10L, "Nuevo", "nuevo@ejemplo.com", "55555555"));

        String cuerpo = """
                {"nombre":"Nuevo","email":"nuevo@ejemplo.com","documento":"55555555"}
                """;

        mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/clientes/10"))
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    void debeDevolverElClienteConSusCuentas() throws Exception {
        ClienteDTO cliente = new ClienteDTO(1L, "Ana Gómez", "ana.gomez@ejemplo.com", "1023456789");
        List<CuentaDTO> cuentas = List.of(
                new CuentaDTO(1L, "4750123456", "AHORRO", new BigDecimal("1500000.00"), 1L));

        when(clienteService.obtenerCuentas(1L))
                .thenReturn(new ClienteConCuentasDTO(cliente, cuentas));

        mockMvc.perform(get("/api/clientes/1/cuentas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cliente.nombre").value("Ana Gómez"))
                .andExpect(jsonPath("$.cuentas[0].numero").value("4750123456"))
                .andExpect(jsonPath("$.cuentas[0].saldo").value(1500000.00));
    }
}
