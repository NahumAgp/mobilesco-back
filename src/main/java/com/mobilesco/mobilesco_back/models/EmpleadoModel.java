package com.mobilesco.mobilesco_back.models;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "empleados")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmpleadoModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // =========================
    // IDENTIDAD
    // =========================

    @Column(nullable = false, length = 80)
    private String nombre;

    @Column(name = "apellido_paterno", nullable = false, length = 80)
    private String apellidoPaterno;

    @Column(name = "apellido_materno", length = 80)
    private String apellidoMaterno;

    // =========================
    // CONTACTO
    // =========================

    @Column(length = 20)
    private String telefono;

    // =========================
    // INFO PERSONAL
    // =========================

    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;

    // =========================
    // FOTO (UNA SOLA)
    // =========================

    @Column(name = "foto_url", length = 300)
    private String fotoUrl;

    // =========================
    // ESTADO
    // =========================

    @Column(nullable = false)
    private Boolean activo;

    // =========================
    // FECHAS (tipo ERP)
    // =========================

    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro;

    @PrePersist
    public void prePersist() {
        if (fechaRegistro == null) {
            fechaRegistro = LocalDateTime.now();
        }
        if (activo == null) {
            activo = true;
        }
    }
}