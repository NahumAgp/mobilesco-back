package com.mobilesco.mobilesco_back.modules.requisicionalmacen.domain.models;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.mobilesco.mobilesco_back.modules.auth.domain.models.UsuarioModel;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
        name = "requisicion_almacen",
        uniqueConstraints = @UniqueConstraint(name = "uk_requisicion_almacen_folio", columnNames = "folio"))
@Getter
@Setter
public class RequisicionAlmacenModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 30, updatable = false)
    private String folio;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "solicitante_usuario_id", nullable = false)
    private UsuarioModel solicitante;

    @Column(name = "solicitante_nombre", nullable = false, length = 180)
    private String solicitanteNombre;

    @Column(name = "destinatario_rol", nullable = false, length = 50)
    private String destinatarioRol;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoRequisicionAlmacen estado;

    @Column(length = 1000)
    private String observaciones;

    @Column(name = "comentario_resolucion", length = 1000)
    private String comentarioResolucion;

    @Column(name = "resuelto_por", length = 190)
    private String resueltoPor;

    @Column(name = "fecha_envio", nullable = false)
    private LocalDateTime fechaEnvio;

    @Column(name = "fecha_resolucion")
    private LocalDateTime fechaResolucion;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;

    @OneToMany(mappedBy = "requisicion", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RequisicionAlmacenDetalleModel> detalles = new ArrayList<>();

    public void agregarDetalle(RequisicionAlmacenDetalleModel detalle) {
        detalle.setRequisicion(this);
        detalles.add(detalle);
    }

    @PrePersist
    protected void prePersist() {
        LocalDateTime ahora = LocalDateTime.now();
        if (fechaEnvio == null) {
            fechaEnvio = ahora;
        }
        fechaActualizacion = ahora;
    }

    @PreUpdate
    protected void preUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }
}
