// RUTA: src/main/java/com/mobilesco/mobilesco_back/dto/linea/LineaCreateDTO.java
/*
 * PATH (direccion): mobilesco-back/src/main/java/com/mobilesco/mobilesco_back/modules/linea/infrastructure/in/api/dtos/LineaCreateDTO.java
 * AUTOR: Nahum Aguilar
 * NOMBRE DE LA CLASE: LineaCreateDTO
 * CONTEXTO: DTO de entrada para crear una linea.
 * NOTAS: Mantiene validaciones de campos requeridos.
 */
package com.mobilesco.mobilesco_back.modules.linea.infrastructure.in.api.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class LineaCreateDTO {

    // Se conserva por compatibilidad con clientes anteriores, pero el servidor genera el codigo.
    private String codigo;
    
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 50, message = "El nombre no puede exceder 50 caracteres")
    private String nombre;
    
    @Size(max = 255, message = "La descripción no puede exceder 255 caracteres")
    private String descripcion;
    
    private Integer orden = 0;
    
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
    
    public Integer getOrden() {
        return orden;
    }
    
    public void setOrden(Integer orden) {
        this.orden = orden;
    }
}
