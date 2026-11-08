package co.edu.poli.banca.cuenta.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import co.edu.poli.banca.cuenta.dto.TitularDTO;
import co.edu.poli.banca.cuenta.exception.ServicioNoDisponibleException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

/**
 * Verifica el contrato HTTP que este servicio espera del cliente-service,
 * sin necesidad de levantar el otro microservicio.
 */
class ClientesClientTest {

    private MockRestServiceServer servidor;
    private ClientesClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        servidor = MockRestServiceServer.bindTo(builder).build();

        RestClient restClient = builder.baseUrl("http://cliente-service:8081").build();
        client = new ClientesClient(restClient, "cuenta-service-test");
    }

    @Test
    void debeConsumirElEndpointDeClientes() {
        String cuerpo = """
                {"id":1,"nombre":"Ana Gómez","email":"ana.gomez@ejemplo.com","documento":"1023456789"}
                """;

        servidor.expect(requestTo("http://cliente-service:8081/api/clientes/1"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(cuerpo, MediaType.APPLICATION_JSON));

        TitularDTO titular = client.obtenerTitular(1L);

        assertThat(titular.nombre()).isEqualTo("Ana Gómez");
        assertThat(titular.email()).isEqualTo("ana.gomez@ejemplo.com");
        servidor.verify();
    }

    @Test
    void debeLanzarServicioNoDisponibleSiElClienteServiceFalla() {
        servidor.expect(requestTo("http://cliente-service:8081/api/clientes/1"))
                .andRespond(withServerError());

        assertThatThrownBy(() -> client.obtenerTitular(1L))
                .isInstanceOf(ServicioNoDisponibleException.class)
                .hasMessageContaining("cliente-service");
    }
}
