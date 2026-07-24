package com.mobilesco.mobilesco_back.modules.requisicionalmacen.infrastructure.in.api.dtos;

import com.mobilesco.mobilesco_back.modules.requisicionalmacen.domain.models.EstadoRequisicionAlmacen;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RequisicionEstadoDTO {

    @NotNull
    private EstadoRequisicionAlmacen estado;

    @Size(max = 1000)
    private String comentario;
}
