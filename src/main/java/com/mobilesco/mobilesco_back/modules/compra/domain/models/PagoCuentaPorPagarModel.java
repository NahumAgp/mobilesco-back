package com.mobilesco.mobilesco_back.modules.compra.domain.models;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "pago_cuenta_por_pagar")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PagoCuentaPorPagarModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cuenta_por_pagar_id", nullable = false, foreignKey = @ForeignKey(name = "fk_pago_cuenta_por_pagar"))
    private CuentaPorPagarModel cuentaPorPagar;

    @Column(name = "fecha_pago", nullable = false)
    private LocalDate fechaPago;

    @Column(name = "monto", nullable = false)
    private Double monto;

    @Column(name = "metodo_pago", length = 60)
    private String metodoPago;

    @Column(name = "referencia", length = 120)
    private String referencia;

    @Column(name = "observaciones", length = 500)
    private String observaciones;

    @Column(name = "usuario", length = 120)
    private String usuario;

    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private LocalDateTime fechaRegistro;

    @PrePersist
    protected void prePersist() {
        fechaRegistro = LocalDateTime.now();
        if (fechaPago == null) {
            fechaPago = LocalDate.now();
        }
    }
}
