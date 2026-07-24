package com.mobilesco.mobilesco_back.modules.notificacion.domain.models;

import java.time.LocalDateTime;

import com.mobilesco.mobilesco_back.modules.auth.domain.models.UsuarioModel;

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
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "notificacion")
@Getter
@Setter
public class NotificacionModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "destinatario_usuario_id", nullable = false)
    private UsuarioModel destinatario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    private TipoNotificacion tipo;

    @Column(nullable = false, length = 180)
    private String titulo;

    @Column(nullable = false, length = 1000)
    private String mensaje;

    @Column(length = 80)
    private String modulo;

    @Column(name = "entidad_tipo", length = 80)
    private String entidadTipo;

    @Column(name = "entidad_id")
    private Long entidadId;

    @Column(length = 500)
    private String ruta;

    @Column(nullable = false)
    private Boolean leida = false;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_lectura")
    private LocalDateTime fechaLectura;

    @PrePersist
    protected void prePersist() {
        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }
        if (leida == null) {
            leida = false;
        }
    }
}
