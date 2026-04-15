package com.mobilesco.mobilesco_back.dto.unidadMedida;

import jakarta.validation.constraints.NotBlank;

public class UnidadMedidaCreateDTO {

    @NotBlank (message = "El nombre es obligatorio")
    private String nombre;

    @NotBlank (message = "El simbolo es obligatorio")
    private String simbolo;
    private String  tipo;

        //Getters & Setterss
    
    public String getNombre() {
        return nombre;
    }
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    public String getSimbolo() {
        return simbolo;
    }
    public void setSimbolo(String simbolo) {
        this.simbolo = simbolo;
    }
    public String getTipo() {
        return tipo;
    }
    public void setTipo(String tipo) {
        this.tipo = tipo;
    }
    
}
