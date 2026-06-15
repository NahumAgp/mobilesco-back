/*
 * PATH (direccion): mobilesco-back/src/main/java/com/mobilesco/mobilesco_back/modules/producto/infrastructure/in/api/dtos/ProductoCreateDTO.java
 * AUTOR: Nahum Aguilar
 * NOMBRE DE LA CLASE: ProductoCreateDTO
 * CONTEXTO: DTO de entrada para crear un producto.
 * NOTAS: Incluye referencias a modelo, nivel, color y material.
 */
package com.mobilesco.mobilesco_back.modules.producto.infrastructure.in.api.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProductoCreateDTO {

    @Size(max = 30, message = "El sku no puede exceder 30 caracteres")
    private String sku;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 200, message = "El nombre no puede exceder 200 caracteres")
    private String nombre;

    @Size(max = 500, message = "La descripcion no puede exceder 500 caracteres")
    private String descripcion;

    @Size(max = 250, message = "La descripcion corta no puede exceder 250 caracteres")
    private String descripcionCorta;

    private Double pesoVolumetrico;

    private Double ancho;

    private Double alto;

    private Double fondo;
    @Size(max = 100, message = "Las dimensiones no pueden exceder 100 caracteres")
    private String dimensiones;

    @DecimalMin(value = "0.0", inclusive = true, message = "El peso volumetrico no puede ser negativo")
    private Double pesoKg;

    @NotNull(message = "El id_modelo es obligatorio")
    @JsonProperty("id_modelo")
    private Long modeloId;

    @NotNull(message = "El id_nivel es obligatorio")
    @JsonProperty("id_nivel")
    private Long nivelId;

    @NotNull(message = "El id_color es obligatorio")
    @JsonProperty("id_color")
    private Long colorId;

    @NotNull(message = "El id_material es obligatorio")
    @JsonProperty("id_material")
    private Long materialId;

    private Boolean activo;
}
