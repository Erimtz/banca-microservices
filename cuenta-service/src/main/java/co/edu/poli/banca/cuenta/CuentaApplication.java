package co.edu.poli.banca.cuenta;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Microservicio de cuentas bancarias.
 *
 * <p>Expone las cuentas y, de forma síncrona, consume al
 * {@code cliente-service} para devolver el titular de cada cuenta.</p>
 */
@SpringBootApplication
public class CuentaApplication {

    public static void main(String[] args) {
        SpringApplication.run(CuentaApplication.class, args);
    }
}
