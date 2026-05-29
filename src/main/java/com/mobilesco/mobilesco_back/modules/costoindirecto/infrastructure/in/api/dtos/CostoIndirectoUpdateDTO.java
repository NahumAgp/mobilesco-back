package com.mobilesco.mobilesco_back.modules.costoindirecto.infrastructure.in.api.dtos;

import com.mobilesco.mobilesco_back.modules.costoindirecto.domain.enums.BaseDistribucion;
import com.mobilesco.mobilesco_back.modules.costoindirecto.domain.enums.PeriodicidadCostoIndirecto;
import com.mobilesco.mobilesco_back.modules.costoindirecto.domain.enums.TipoCostoIndirecto;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CostoIndirectoUpdateDTO {
    
    @Size(max = 20, message = "El código no puede exceder 20 caracteres")
    private String codigo;
    
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    private String nombre;
    
    @Size(max = 255, message = "La descripción no puede exceder 255 caracteres")
    private String descripcion;
    
    private TipoCostoIndirecto tipo;
    private BaseDistribucion baseDistribucion;
    
    @Positive(message = "El monto mensual debe ser mayor a 0")
    private Double montoMensual;
    private PeriodicidadCostoIndirecto periodicidad;
    
    @Positive(message = "El porcentaje debe ser mayor a 0")
    private Double porcentajeAsignado;
    
    @Positive(message = "La tasa variable debe ser mayor a 0")
    private Double tasaVariable;
    
    private Boolean activo;
}
