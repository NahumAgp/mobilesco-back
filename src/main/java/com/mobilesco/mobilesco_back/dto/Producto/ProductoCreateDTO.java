package com.mobilesco.mobilesco_back.dto.Producto;

import com.fasterxml.jackson.annotation.JsonProperty;

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

    @NotNull(message = "El id_modelo es obligatorio")
    @JsonProperty("id_modelo")
    private Long modeloId;

    @NotNull(message = "El id_nivel es obligatorio")
    @JsonProperty("id_nivel")
    private Long nivelId;

    @NotNull(message = "El id_color es obligatorio")
    @JsonProperty("id_color")
    private Long colorId;

    private Boolean activo;
}
