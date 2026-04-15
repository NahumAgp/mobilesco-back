package com.mobilesco.mobilesco_back.dto.TipoProducto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TipoProductoResponseDTO {
    private Long id;
    private String nombre;
    private String descripcion;
    private Long familiaId;
    private String familiaNombre;
    private String familiaRuta; // Opcional: para mostrar la jerarquía
    private Boolean activo;
    private LocalDateTime fechaRegistro;
    private LocalDateTime fechaActualizacion;
}