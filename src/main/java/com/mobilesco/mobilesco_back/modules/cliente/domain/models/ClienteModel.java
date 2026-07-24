package com.mobilesco.mobilesco_back.modules.cliente.domain.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "cliente",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_cliente_codigo", columnNames = "codigo"),
                @UniqueConstraint(name = "uk_cliente_rfc", columnNames = "rfc")
        })
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClienteModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20, updatable = false)
    private String codigo;

    @Enumerated(EnumType.STRING)
    @Column(name = "clasificacion", nullable = false, length = 20)
    private ClasificacionCliente clasificacion;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_persona", nullable = false, length = 10)
    private TipoPersonaCliente tipoPersona;

    @Column(length = 150)
    private String nombre;

    @Column(name = "razon_social", length = 180)
    private String razonSocial;

    @Column(name = "nombre_comercial", length = 180)
    private String nombreComercial;

    @Column(length = 13)
    private String rfc;

    @Column(name = "contacto_nombre", length = 150)
    private String contactoNombre;

    @Column(length = 150)
    private String correo;

    @Column(length = 25)
    private String telefono;

    @Column(length = 25)
    private String whatsapp;

    @Column(length = 120)
    private String estado;

    @Column(length = 120)
    private String ciudad;

    @Column(length = 120)
    private String colonia;

    @Column(length = 180)
    private String calle;

    @Column(name = "numero_exterior", length = 20)
    private String numeroExterior;

    @Column(name = "numero_interior", length = 20)
    private String numeroInterior;

    @Column(name = "codigo_postal", length = 10)
    private String codigoPostal;

    @Column(name = "dias_credito", nullable = false)
    @Builder.Default
    private Integer diasCredito = 0;

    @Column(name = "limite_credito", nullable = false, precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal limiteCredito = BigDecimal.ZERO;

    @Column(length = 1000)
    private String notas;

    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;

    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private LocalDateTime fechaRegistro;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;

    @PrePersist
    protected void prePersist() {
        LocalDateTime ahora = LocalDateTime.now();
        fechaRegistro = ahora;
        fechaActualizacion = ahora;
    }

    @PreUpdate
    protected void preUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }
}
