package co.edu.poli.banca.cliente.config;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;
import org.springframework.boot.http.client.HttpClientSettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * Configuración del {@link RestClient} usado para consumir al {@code cuenta-service}.
 *
 * <p>La base URL se inyecta desde la propiedad {@code servicio.cuentas.base-url},
 * lo que permite apuntar a {@code http://localhost:8082} en desarrollo y a
 * {@code http://cuenta-service:8082} dentro de la red de Docker.</p>
 */
@Configuration
public class RestClientConfig {

    @Bean
    @Qualifier("cuentasRestClient")
    public RestClient cuentasRestClient(
            @Value("${servicio.cuentas.base-url}") String baseUrl,
            @Value("${servicio.cuentas.timeout-ms:3000}") long timeoutMs) {

        Duration timeout = Duration.ofMillis(timeoutMs);
        HttpClientSettings settings = HttpClientSettings.defaults().withTimeouts(timeout, timeout);

        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(ClientHttpRequestFactoryBuilder.detect().build(settings))
                .build();
    }
}
