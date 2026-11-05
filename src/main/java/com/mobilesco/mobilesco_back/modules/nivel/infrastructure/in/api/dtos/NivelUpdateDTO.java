package com.mobilesco.mobilesco_back.modules.nivel.infrastructure.in.api.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class NivelUpdateDTO {

    @NotBlank(message = "El codigo es obligatorio")
    @Pattern(regexp = "^[A-Z0-9]{1,3}$", message = "El codigo debe tener de 1 a 3 letras o numeros")
    private String codigo;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 50, message = "El nombre no puede exceder 50 caracteres")
    private String nombre;

    @Size(max = 255, message = "La descripcion no puede exceder 255 caracteres")
    private String descripcion;

    private Boolean activo;
    private Boolean costoInicialPendiente;

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

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public Boolean getCostoInicialPendiente() {
        return costoInicialPendiente;
    }

    public void setCostoInicialPendiente(Boolean costoInicialPendiente) {
        this.costoInicialPendiente = costoInicialPendiente;
    }
}
