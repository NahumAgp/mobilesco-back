package com.mobilesco.mobilesco_back.modules.salidainsumo.infrastructure.in.api.dtos;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SalidaInsumoCreateDTO {

    @NotBlank(message = "El tipo de salida es obligatorio")
    @Size(max = 20, message = "El tipo de salida no puede exceder 20 caracteres")
    private String tipoSalida;

    @Size(max = 100, message = "La orden de producción no puede exceder 100 caracteres")
    private String ordenProduccion;

    private LocalDateTime fechaSalida;

    @Size(max = 500, message = "Las observaciones no pueden exceder 500 caracteres")
    private String observaciones;

    @NotBlank(message = "La persona responsable es obligatoria")
    @Size(max = 150, message = "La persona responsable no puede exceder 150 caracteres")
    private String responsable;

    @Size(max = 120, message = "El area no puede exceder 120 caracteres")
    private String area;

    @NotNull(message = "Debe incluir al menos un detalle")
    private List<DetalleSalidaInsumoCreateDTO> detalles;
}
