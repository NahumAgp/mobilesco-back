package com.mobilesco.mobilesco_back.dto.Produccion;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProduccionInsumoListaDTO {
    
    @NotEmpty(message = "Debe incluir al menos un insumo")
    private List<ProduccionInsumoDTO> insumos;
}