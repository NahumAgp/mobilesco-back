/*
 * PATH (direccion): mobilesco-back/src/main/java/com/mobilesco/mobilesco_back/modules/familia/domain/models/FamiliaModel.java
 * AUTOR: Nahum Aguilar
 * NOMBRE DE LA CLASE: FamiliaModel
 * CONTEXTO: Entidad JPA del modulo Familia para catalogo de familias de producto.
 * NOTAS: Mantener relacion ManyToOne con LineaModel hasta su futura modularizacion.
 */
package com.mobilesco.mobilesco_back.modules.familia.domain.models;

import java.time.LocalDateTime;

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

import com.mobilesco.mobilesco_back.modules.linea.domain.models.LineaModel;

@Entity
@Getter
@Setter
@Table(
    name = "familias",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_familia_linea_codigo", columnNames = {"linea_id", "codigo"}),
        @UniqueConstraint(name = "uk_familia_linea_nombre", columnNames = {"linea_id", "nombre"})
    }
)
public class FamiliaModel {
    
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
    @JoinColumn(name = "linea_id")
    private LineaModel linea;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
