package com.mobilesco.mobilesco_back.modules.requisicionalmacen.infrastructure.in.api.dtos;

import java.time.LocalDateTime;
import java.util.List;

import com.mobilesco.mobilesco_back.modules.requisicionalmacen.domain.models.EstadoRequisicionAlmacen;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RequisicionResponseDTO {
    private Long id;
    private String folio;
    private Long solicitanteUsuarioId;
    private String solicitanteNombre;
    private String destinatario;
    private EstadoRequisicionAlmacen estado;
    private String estadoEtiqueta;
    private String observaciones;
    private String comentarioResolucion;
    private String resueltoPor;
    private LocalDateTime fechaEnvio;
    private LocalDateTime fechaResolucion;
    private LocalDateTime fechaActualizacion;
    private Integer totalPartidas;
    private List<RequisicionDetalleResponseDTO> partidas;
}
