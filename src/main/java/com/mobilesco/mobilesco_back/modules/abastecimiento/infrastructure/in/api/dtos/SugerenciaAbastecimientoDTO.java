package com.mobilesco.mobilesco_back.modules.abastecimiento.infrastructure.in.api.dtos;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SugerenciaAbastecimientoDTO {
    private Long insumoId;
    private String codigo;
    private String nombre;
    private Long unidadMedidaId;
    private String unidadMedidaSimbolo;
    private String clasificacionAbc;
    private Double consumoMensual;
    private Double stockDisponible;
    private Double stockMinimo;
    private Double puntoReorden;
    private Double cantidadSugerida;
    private String prioridad;
    private String explicacion;
    private ProveedorAbastecimientoDTO proveedorSugerido;
    private List<ProveedorAbastecimientoDTO> proveedores;
}
