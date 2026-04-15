package com.mobilesco.mobilesco_back.dto.CostoIndirecto;

import com.mobilesco.mobilesco_back.enums.BaseDistribucion;
import com.mobilesco.mobilesco_back.enums.TipoCostoIndirecto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CostoIndirectoCreateDTO {
    
    @NotBlank(message = "El código es obligatorio")
    @Size(max = 20, message = "El código no puede exceder 20 caracteres")
    private String codigo;
    
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    private String nombre;
    
    @Size(max = 255, message = "La descripción no puede exceder 255 caracteres")
    private String descripcion;
    
    @NotNull(message = "El tipo de costo es obligatorio")
    private TipoCostoIndirecto tipo;
    
    @NotNull(message = "La base de distribución es obligatoria")
    private BaseDistribucion baseDistribucion;
    
    @Positive(message = "El monto mensual debe ser mayor a 0")
    private Double montoMensual;        // Para costos fijos
    
    @Positive(message = "El porcentaje debe ser mayor a 0")
    private Double porcentajeAsignado;   // Para costos fijos por producto
    
    @Positive(message = "La tasa variable debe ser mayor a 0")
    private Double tasaVariable;         // Para costos variables
}