package com.mobilesco.mobilesco_back.modules.configuracion.infrastructure.in.api.dtos;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ConfiguracionSistemaDTO {
    @NotBlank @Size(max = 160)
    private String nombreEmpresa;
    @Size(max = 180)
    private String razonSocial;
    @Size(max = 20)
    private String rfc;
    @Size(max = 160)
    private String correo;
    @Size(max = 40)
    private String telefono;
    @Size(max = 255)
    private String direccion;
    @Size(max = 180)
    private String sitioWeb;
    @NotBlank @Pattern(regexp = "MXN|USD|EUR")
    private String moneda;
    @NotBlank @Pattern(regexp = "#[0-9A-Fa-f]{6}")
    private String colorPrimario;
    private String logoSistemaUrl;
    private String logoDocumentosUrl;
    private String logoCatalogoUrl;
    @DecimalMin("0.25") @DecimalMax("24")
    private double coberturaAbcA;
    @DecimalMin("0.25") @DecimalMax("24")
    private double coberturaAbcB;
    @DecimalMin("0.25") @DecimalMax("24")
    private double coberturaAbcC;
}
