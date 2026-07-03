package com.mobilesco.mobilesco_back.modules.empleado.infrastructure.in.api.dtos;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmpleadoResponseDTO {

    private Long id;

    private String nombre;
    private String apellidoPaterno;
    private String apellidoMaterno;

    private String telefono;

    private LocalDate fechaNacimiento;

    private String fotoUrl;

    private Long areaId;
    private String areaNombre;

    private Boolean activo;

    private String correo;
    private Boolean tieneCuenta;

    private LocalDateTime fechaRegistro;
    
}
