package com.mobilesco.mobilesco_back.dto.Producto;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProductoUpdateDTO {

    @NotBlank(message = "El sku es obligatorio")
    @Size(max = 30, message = "El sku no puede exceder 30 caracteres")
    private String sku;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 200, message = "El nombre no puede exceder 200 caracteres")
    private String nombre;

    @Size(max = 500, message = "La descripcion no puede exceder 500 caracteres")
    private String descripcion;

    @JsonProperty("id_modelo")
    private Long modeloId;

    @JsonProperty("id_nivel")
    private Long nivelId;

    @JsonProperty("id_color")
    private Long colorId;

    private Boolean activo;
}
