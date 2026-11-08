package co.edu.poli.banca.cuenta.dto;

/**
 * Vista del cliente titular tal como la devuelve el {@code cliente-service}.
 *
 * <p>DTO copiado en ambos microservicios: cada servicio define su propio
 * contrato y no comparte clases con el otro.</p>
 *
 * @param id        identificador del cliente
 * @param nombre    nombre completo
 * @param email     correo electrónico
 * @param documento número de documento de identidad
 */
public record TitularDTO(Long id, String nombre, String email, String documento) {
}
