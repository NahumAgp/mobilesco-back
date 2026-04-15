package com.mobilesco.mobilesco_back.models;

import java.time.LocalDate;

import com.mobilesco.mobilesco_back.enums.TipoInsumo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "proveedor")
public class ProveedorModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // IDENTIDAD 
    private String razonSocial;
    private String rfc;
    
    // NOMBRE
    private String nombre;
    private String apellidoPaterno;
    private String apellidoMaterno;
    
    // DIRECCION
    private String estado;
    private String ciudad;
    private String colonia;
    private String calle;
    private String numeroExterior;
    private String numeroInterior;
    private String codigoPostal;

    @Enumerated(EnumType.STRING)
    private TipoInsumo tipoInsumo;

    // CONTACTO
    private String telefono;
    private String correo;

    // FECHAS
    @Column(name = "fecha_ultimo_contacto")
    private LocalDate fechaUltimoContacto;

    @Column(name = "fecha_registro")
    private LocalDate fechaRegistro;

    // ESTADO
    @Column(name = "activo")
    private Boolean activo;
    
    @PrePersist
    protected void onCreate() {
        this.fechaRegistro = LocalDate.now();
    }
    
    
}