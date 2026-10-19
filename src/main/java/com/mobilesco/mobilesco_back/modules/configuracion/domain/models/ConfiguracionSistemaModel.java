package com.mobilesco.mobilesco_back.modules.configuracion.domain.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "configuracion_sistema")
@Getter
@Setter
public class ConfiguracionSistemaModel {
    @Id
    private Long id;

    @Column(name = "nombre_empresa", nullable = false, length = 160)
    private String nombreEmpresa;
    @Column(name = "razon_social", length = 180)
    private String razonSocial;
    @Column(length = 20)
    private String rfc;
    @Column(length = 160)
    private String correo;
    @Column(length = 40)
    private String telefono;
    @Column(length = 255)
    private String direccion;
    @Column(name = "sitio_web", length = 180)
    private String sitioWeb;
    @Column(nullable = false, length = 3)
    private String moneda;
    @Column(name = "color_primario", nullable = false, length = 7)
    private String colorPrimario;
    @Column(name = "logo_sistema_url", length = 255)
    private String logoSistemaUrl;
    @Column(name = "logo_documentos_url", length = 255)
    private String logoDocumentosUrl;
    @Column(name = "logo_catalogo_url", length = 255)
    private String logoCatalogoUrl;
    @Column(name = "cobertura_abc_a", nullable = false)
    private double coberturaAbcA;
    @Column(name = "cobertura_abc_b", nullable = false)
    private double coberturaAbcB;
    @Column(name = "cobertura_abc_c", nullable = false)
    private double coberturaAbcC;
}
