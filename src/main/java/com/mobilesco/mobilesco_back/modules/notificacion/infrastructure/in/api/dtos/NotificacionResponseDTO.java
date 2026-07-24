package com.mobilesco.mobilesco_back.modules.notificacion.infrastructure.in.api.dtos;

import java.time.LocalDateTime;

import com.mobilesco.mobilesco_back.modules.notificacion.domain.models.TipoNotificacion;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NotificacionResponseDTO {
    private Long id;
    private TipoNotificacion tipo;
    private String titulo;
    private String mensaje;
    private String modulo;
    private String entidadTipo;
    private Long entidadId;
    private String ruta;
    private Boolean leida;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaLectura;
}
