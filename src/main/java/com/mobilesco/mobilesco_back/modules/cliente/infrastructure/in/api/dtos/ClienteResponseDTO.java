package com.mobilesco.mobilesco_back.modules.cliente.infrastructure.in.api.dtos;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.mobilesco.mobilesco_back.modules.cliente.domain.models.ClasificacionCliente;
import com.mobilesco.mobilesco_back.modules.cliente.domain.models.TipoPersonaCliente;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ClienteResponseDTO {
    private Long id;
    private String codigo;
    private ClasificacionCliente clasificacion;
    private String clasificacionEtiqueta;
    private TipoPersonaCliente tipoPersona;
    private String tipoPersonaEtiqueta;
    private String nombre;
    private String razonSocial;
    private String nombreComercial;
    private String nombreVisual;
    private String rfc;
    private String contactoNombre;
    private String correo;
    private String telefono;
    private String whatsapp;
    private String estado;
    private String ciudad;
    private String colonia;
    private String calle;
    private String numeroExterior;
    private String numeroInterior;
    private String codigoPostal;
    private Integer diasCredito;
    private BigDecimal limiteCredito;
    private String notas;
    private Boolean activo;
    private LocalDateTime fechaRegistro;
    private LocalDateTime fechaActualizacion;
}
