package co.edu.poli.banca.cuenta.repository;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

/**
 * Entidad JPA de una cuenta bancaria.
 *
 * <p>Se persiste en H2 (modo archivo) mediante Hibernate. El esquema lo crea
 * Hibernate con {@code spring.jpa.hibernate.ddl-auto: update} en el primer
 * arrancado y lo conserva en el volumen {@code cuenta-data}.</p>
 */
@Entity
@Table(name = "cuentas")
public class CuentaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero", nullable = false)
    private String numero;

    @Column(name = "tipo", nullable = false)
    private String tipo;

    @Column(name = "saldo", precision = 19, scale = 2, nullable = false)
    private BigDecimal saldo;

    @Column(name = "cliente_id", nullable = false)
    private Long clienteId;

    protected CuentaEntity() {
        // JPA necesita un constructor sin argumentos
    }

    public CuentaEntity(Long id, String numero, String tipo, BigDecimal saldo, Long clienteId) {
        this.id = id;
        this.numero = numero;
        this.tipo = tipo;
        this.saldo = saldo;
        this.clienteId = clienteId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public BigDecimal getSaldo() {
        return saldo;
    }

    public void setSaldo(BigDecimal saldo) {
        this.saldo = saldo;
    }

    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }
}
