package com.mobilesco.mobilesco_back.modules.subfamilia.domain.models;

import java.time.LocalDateTime;

import com.mobilesco.mobilesco_back.modules.familia.domain.models.FamiliaModel;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(
    name = "subfamilias",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_subfamilia_familia_codigo", columnNames = {"familia_id", "codigo"}),
        @UniqueConstraint(name = "uk_subfamilia_familia_nombre", columnNames = {"familia_id", "nombre"})
    }
)
public class SubfamiliaModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 10)
    private String codigo;

    @Column(nullable = false, length = 100)
    private String nombre;

    private String descripcion;

    @Column(name = "activo")
    private Boolean activo = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "familia_id", nullable = false)
    private FamiliaModel familia;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (activo == null) {
            activo = true;
        }
    }
}
