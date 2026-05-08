package com.mobilesco.mobilesco_back.dto.Insumo;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class InsumoCreateDTO {
    
    @Size(max = 150, message = "El codigo no puede exceder 150 caracteres")
    private String codigo;

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
    
    private Double stockActual;

    @NotNull(message = "El stock mínimo es obligatorio")
    private Double stockMinimo;
}
