package co.edu.poli.banca.cliente.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import co.edu.poli.banca.cliente.dto.CuentaDTO;
import co.edu.poli.banca.cliente.exception.ServicioNoDisponibleException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

/**
 * Verifica el contrato HTTP que este servicio espera del cuenta-service,
 * sin necesidad de levantar el otro microservicio.
 */
class CuentasClientTest {

    private MockRestServiceServer servidor;
    private CuentasClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        servidor = MockRestServiceServer.bindTo(builder).build();

        RestClient restClient = builder.baseUrl("http://cuenta-service:8082").build();
        client = new CuentasClient(restClient, "cliente-service-test");
    }

    @Test
    void debeConsumirElEndpointDeCuentasPorCliente() {
        String cuerpo = """
                [{"id":1,"numero":"4750123456","tipo":"AHORRO","saldo":1500000.00,"clienteId":1}]
                """;

        servidor.expect(requestTo("http://cuenta-service:8082/api/cuentas/cliente/1"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(cuerpo, MediaType.APPLICATION_JSON));

        List<CuentaDTO> cuentas = client.listarPorCliente(1L);

        assertThat(cuentas).hasSize(1);
        assertThat(cuentas.get(0).numero()).isEqualTo("4750123456");
        servidor.verify();
    }

    @Test
    void debeLanzarServicioNoDisponibleSiElCuentaServiceFalla() {
        servidor.expect(requestTo("http://cuenta-service:8082/api/cuentas/cliente/1"))
                .andRespond(withServerError());

        assertThatThrownBy(() -> client.listarPorCliente(1L))
                .isInstanceOf(ServicioNoDisponibleException.class)
                .hasMessageContaining("cuenta-service");
    }
}
