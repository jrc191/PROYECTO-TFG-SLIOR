package com.slior.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidad que representa una dirección física en la base de datos local.
 * Alimentada por datos de Catastro u OSM.
 */
@Entity
@Table(name = "direcciones")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Direccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    private String numero;

    private String municipio;

    private String provincia;

    @Column(name = "codigo_postal")
    private String codigoPostal;

    private Double latitud;

    private Double longitud;
}

