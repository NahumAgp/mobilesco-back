package com.mobilesco.mobilesco_back.modules.cliente.infrastructure.in.api.dtos;

import java.math.BigDecimal;

import com.mobilesco.mobilesco_back.modules.cliente.domain.models.ClasificacionCliente;
import com.mobilesco.mobilesco_back.modules.cliente.domain.models.TipoPersonaCliente;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ClienteRequestDTO {

    @NotNull(message = "La clasificación comercial es obligatoria")
    private ClasificacionCliente clasificacion;

    @NotNull(message = "El tipo de persona es obligatorio")
    private TipoPersonaCliente tipoPersona;

    @Size(max = 150)
    private String nombre;

    @Size(max = 180)
    private String razonSocial;

    @Size(max = 180)
    private String nombreComercial;

    @Pattern(
            regexp = "^$|^[A-Za-zÑñ&]{3,4}[0-9]{6}[A-Za-z0-9]{3}$",
            message = "El RFC debe tener un formato válido de 12 o 13 caracteres")
    private String rfc;

    @Size(max = 150)
    private String contactoNombre;

    @Email(message = "El correo no tiene un formato válido")
    @Size(max = 150)
    private String correo;

    @Size(max = 25)
    private String telefono;

    @Size(max = 25)
    private String whatsapp;

    @Size(max = 120)
    private String estado;

    @Size(max = 120)
    private String ciudad;

    @Size(max = 120)
    private String colonia;

    @Size(max = 180)
    private String calle;

    @Size(max = 20)
    private String numeroExterior;

    @Size(max = 20)
    private String numeroInterior;

    @Size(max = 10)
    private String codigoPostal;

    @Min(value = 0, message = "Los días de crédito no pueden ser negativos")
    private Integer diasCredito = 0;

    @PositiveOrZero(message = "El límite de crédito no puede ser negativo")
    private BigDecimal limiteCredito = BigDecimal.ZERO;

    @Size(max = 1000)
    private String notas;

    private Boolean activo = true;

    @AssertTrue(message = "Captura el nombre o la razón social del cliente")
    public boolean isIdentidadValida() {
        return tieneTexto(nombre) || tieneTexto(razonSocial) || tieneTexto(nombreComercial);
    }

    private boolean tieneTexto(String value) {
        return value != null && !value.isBlank();
    }
}
