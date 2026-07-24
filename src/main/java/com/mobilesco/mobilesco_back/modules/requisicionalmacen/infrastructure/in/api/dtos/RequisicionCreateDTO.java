package com.mobilesco.mobilesco_back.modules.requisicionalmacen.infrastructure.in.api.dtos;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RequisicionCreateDTO {

    @Size(max = 1000)
    private String observaciones;

    @Valid
    @NotEmpty(message = "Agrega al menos un insumo a la requisición")
    private List<RequisicionPartidaRequestDTO> partidas = new ArrayList<>();
}
