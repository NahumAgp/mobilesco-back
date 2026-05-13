package com.mobilesco.mobilesco_back.dto.material;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class MaterialCreateDTO {

    @NotBlank(message = "El codigo es obligatorio")
    @Pattern(regexp = "^[A-Z0-9]{1,10}$", message = "El codigo solo puede contener mayusculas y numeros (maximo 10)")
    private String codigo;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    private String nombre;

    @Size(max = 255, message = "La descripcion no puede exceder 255 caracteres")
    private String descripcion;
}
