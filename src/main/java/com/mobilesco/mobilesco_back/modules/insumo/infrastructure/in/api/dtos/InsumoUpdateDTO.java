package com.mobilesco.mobilesco_back.modules.insumo.infrastructure.in.api.dtos;

import com.fasterxml.jackson.annotation.JsonAlias;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class InsumoUpdateDTO {
    
    @Size(max = 150, message = "El codigo no puede exceder 150 caracteres")
    private String codigo;

    @Size(max = 150, message = "El nombre no puede exceder 150 caracteres")
    private String nombre;
    
    @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
    private String descripcion;
    
    @NotNull(message = "La unidad de medida es obligatoria")
    private Long unidadMedidaId;

    @Size(max = 80, message = "El tipo de insumo no puede exceder 80 caracteres")
    private String tipoInsumo;

    @JsonAlias("costo_cotizar")
    @PositiveOrZero(message = "El costo de cotizacion no puede ser negativo")
    private Double costoCotizacion;
    
    @NotNull(message = "El stock mínimo es obligatorio")
    private Double stockMinimo;

    //Ubicacion del insumo
    private String ubicacion;
    private String fila;
    private String columna;
    
    private Double stockActual;
    
    private Boolean activo;
}
