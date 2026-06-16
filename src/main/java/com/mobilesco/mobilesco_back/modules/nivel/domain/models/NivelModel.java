package com.mobilesco.mobilesco_back.modules.nivel.domain.models;
// RUTA: src/main/java/com/mobilesco/mobilesco_back/models/NivelModel.java

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

import com.mobilesco.mobilesco_back.modules.modelo.domain.models.ModeloModel;
import com.mobilesco.mobilesco_back.modules.categoria.domain.models.CategoriaModel;

@Entity
@Getter
@Setter
@Table(
    name = "niveles",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_nivel_modelo_categoria", columnNames = {"producto_base_id", "categoria_id"})
    }
)
public class NivelModel {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false, length = 3)
    private String codigo;
    
    @Column(nullable = false, length = 50)
    private String nombre;

    @ManyToOne
    @JoinColumn(name = "producto_base_id")
    private ModeloModel modelo;

    @ManyToOne
    @JoinColumn(name = "categoria_id")
    private CategoriaModel categoria;
    
    @Column(length = 255)
    private String descripcion;
    
    @Column(name = "activo")
    private Boolean activo = true;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
