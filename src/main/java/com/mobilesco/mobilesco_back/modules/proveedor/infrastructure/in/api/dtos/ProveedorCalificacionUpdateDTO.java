package com.mobilesco.mobilesco_back.modules.proveedor.infrastructure.in.api.dtos;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.AssertTrue;

public class ProveedorCalificacionUpdateDTO {

    @DecimalMin(value = "0.00", message = "La calificación del proveedor debe ser mayor o igual a 0")
    @DecimalMax(value = "100.00", message = "La calificación del proveedor debe ser menor o igual a 100")
    @Digits(integer = 3, fraction = 2, message = "La calificación del proveedor admite hasta 2 decimales")
    private BigDecimal calificacionProveedor;
    private boolean calificacionProveedorDefinida;

    public BigDecimal getCalificacionProveedor() {
        return calificacionProveedor;
    }

    public void setCalificacionProveedor(BigDecimal calificacionProveedor) {
        this.calificacionProveedor = calificacionProveedor;
        this.calificacionProveedorDefinida = true;
    }

    @JsonIgnore
    @AssertTrue(message = "Incluye el campo calificacionProveedor")
    public boolean isCalificacionProveedorDefinida() {
        return calificacionProveedorDefinida;
    }
}
