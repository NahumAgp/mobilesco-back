/*
 * PATH (direccion): mobilesco-back/src/main/java/com/mobilesco/mobilesco_back/modules/color/infrastructure/in/api/dtos/ColorCreateDTO.java
 * AUTOR: Nahum Aguilar
 * NOMBRE DE LA CLASE: ColorCreateDTO
 * CONTEXTO: DTO de entrada para crear un color.
 * NOTAS: Incluye validacion de codigo y formato hexadecimal.
 */
package com.mobilesco.mobilesco_back.modules.color.infrastructure.in.api.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class ColorCreateDTO {

    // Se conserva por compatibilidad; el servidor genera el codigo.
    private String codigo;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 30, message = "El nombre no puede exceder 30 caracteres")
    private String nombre;

    @Size(max = 255, message = "La descripcion no puede exceder 255 caracteres")
    private String descripcion;

    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "El cÃ³digo hexadecimal debe tener formato #RRGGBB")
    private String hex;

    // Getters y Setters
    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getHex() {
        return hex;
    }

    public void setHex(String hex) {
        this.hex = hex;
    }
}
