package com.mobilesco.mobilesco_back.modules.modelo.infrastructure.in.api.dtos;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModeloInsumoDTO {
    @JsonAlias({"insumoId", "insumo_id"})
    private Long id;
    private String codigo;
    private String nombre;
    private String unidadMedida;
    private String tipoInsumo;
    @JsonAlias({"materialId", "material_id"})
    private Long materialId;
    private String materialCodigo;
    private String materialNombre;
    @JsonProperty("cantidad")
    private Double cantidad;
    @JsonAlias({"desperdicio", "desperdicio_porcentaje"})
    private Double desperdicioPorcentaje;
    @JsonAlias({"costo", "costo_cotizar", "costo_cotizacion"})
    private Double costoCotizacion;
    private Boolean activo;
    @Builder.Default
    private Boolean conjunto = Boolean.FALSE;
    private java.util.List<com.mobilesco.mobilesco_back.modules.insumo.infrastructure.in.api.dtos.ConjuntoInsumoDTO.Componente> componentes;

    public Boolean getConjunto() {
        return Boolean.TRUE.equals(conjunto);
    }

    public void setConjunto(Boolean conjunto) {
        this.conjunto = Boolean.TRUE.equals(conjunto);
    }
}
