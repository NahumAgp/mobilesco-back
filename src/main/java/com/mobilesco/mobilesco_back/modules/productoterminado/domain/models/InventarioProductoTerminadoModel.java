package com.mobilesco.mobilesco_back.modules.productoterminado.domain.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.mobilesco.mobilesco_back.modules.producto.domain.models.ProductoModel;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "inventario_producto_terminado")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class InventarioProductoTerminadoModel {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne(optional = false) @JoinColumn(name = "producto_id", nullable = false, unique = true)
    private ProductoModel producto;
    @Column(nullable = false, precision = 14, scale = 3)
    private BigDecimal existencia;
    @Column(nullable = false, precision = 14, scale = 3)
    private BigDecimal reservado;
    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;
    @PrePersist @PreUpdate void touch() { fechaActualizacion = LocalDateTime.now(); }
}
