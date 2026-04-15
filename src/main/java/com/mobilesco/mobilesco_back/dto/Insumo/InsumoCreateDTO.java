package com.mobilesco.mobilesco_back.dto.Insumo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class InsumoCreateDTO {
    
    @NotBlank(message= "El Codigo es obligatorio")
    private Double codigo;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 150, message = "El nombre no puede exceder 150 caracteres")
    private String nombre;
    
    @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
    private String descripcion;
    
    @NotNull(message = "La unidad de medida es obligatoria")
    private Long unidadMedidaId;  // Unidad de CONSUMO

    private Double costo_cotizar;

    //Ubicacion del insumo
    private String ubicacion;
    private String fila;
    private String columna;
    
    @Positive(message = "El stock mínimo debe ser mayor a 0")
    private Double stockMinimo;
}