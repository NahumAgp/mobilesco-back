package com.mobilesco.mobilesco_back.dto.Insumo;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InsumoResponseDTO {
    private Long id;
    private String nombre;
    private String descripcion;
    
    // Datos de unidad de medida (consumo)
    private Long unidadMedidaId;
    private String unidadMedidaNombre;
    private String unidadMedidaSimbolo;
    
    private Double stockActual;
    private Double stockMinimo;
    private Boolean activo;
    private LocalDateTime fechaRegistro;
    private LocalDateTime fechaActualizacion;

    //Ubicacion del insumo
    private String ubicacion;
    private String fila;
    private String columna;
    
    // NOTA: El costo NO va aquí, se obtiene de compras/kardex
}