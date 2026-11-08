package co.edu.poli.banca.cliente;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Microservicio de clientes.
 *
 * <p>Expone la información de los clientes y, de forma síncrona, consume al
 * {@code cuenta-service} para devolver las cuentas asociadas a cada cliente.</p>
 */
@SpringBootApplication
public class ClienteApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClienteApplication.class, args);
    }
}
