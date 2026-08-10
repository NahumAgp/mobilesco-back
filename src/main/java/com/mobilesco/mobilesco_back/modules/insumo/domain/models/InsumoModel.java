package com.mobilesco.mobilesco_back.modules.insumo.domain.models;

import java.time.LocalDateTime;

import com.mobilesco.mobilesco_back.modules.insumo.domain.enums.TipoInsumo;
import com.mobilesco.mobilesco_back.modules.unidadmedida.domain.models.UnidadMedidaModel;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "insumo",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_insumo_nombre", columnNames = {"nombre"})
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InsumoModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo", nullable = false, length = 150)
    private String codigo;

    @Column(name = "codigo_barras", unique = true, length = 13)
    private String codigoBarras;

    @Column(name = "nombre", nullable = false, length = 150)
    private String nombre;

    @Column(name = "descripcion", length = 500)
    private String descripcion;

    //Ubicacion del insumo
    private String ubicacion;
    private String fila;
    private String columna;

    @Column(name = "costo_cotizar")
    private Double costoCotizacion;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_insumo")
    private TipoInsumo tipoInsumo;


    // Unidad de Medida
    @ManyToOne
    @JoinColumn(name = "unidad_medida_id", nullable = false, 
                foreignKey = @ForeignKey(name = "fk_insumo_unidad_medida"))
    private UnidadMedidaModel unidadMedida;  

    // Stock (siempre en unidad de consumo)
    @Column(name = "stock_actual", nullable = false)
    @Builder.Default
    private Double stockActual = 0.0;

    /** Existencia comprometida con ordenes de produccion liberadas. */
    @Column(name = "stock_apartado", nullable = false)
    @Builder.Default
    private Double stockApartado = 0.0;

    @Column(name = "stock_minimo")
    private Double stockMinimo;

    // Estado
    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;

    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private LocalDateTime fechaRegistro;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;

    @Version
    private Long version;

    @PrePersist
    protected void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        fechaRegistro = now;
        fechaActualizacion = now;
    }

    @PreUpdate
    protected void preUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }
}
