package com.mobilesco.mobilesco_back.modules.tipoinsumo.domain.models;

import java.text.Normalizer;
import java.time.LocalDate;
import java.util.Locale;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "tipo_insumo_catalogo")
public class TipoInsumoModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
        nullable = false,
        unique = true,
        length = 80,
        columnDefinition = "VARCHAR(80) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci"
    )
    private String codigo;

    @Column(
        nullable = false,
        length = 120,
        columnDefinition = "VARCHAR(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci"
    )
    private String nombre;

    @Column(
        name = "nombre_normalizado",
        nullable = false,
        unique = true,
        length = 120,
        columnDefinition = "VARCHAR(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci"
    )
    private String nombreNormalizado;

    @Column(length = 255, columnDefinition = "VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String descripcion;

    @Column(nullable = false)
    private Boolean activo = true;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDate fechaRegistro;

    @PrePersist
    @PreUpdate
    protected void prepararPersistencia() {
        if (nombre != null) {
            nombre = limpiarTexto(nombre);
            nombreNormalizado = normalizarNombre(nombre);
        }

        if (codigo != null) {
            codigo = codigo.trim().toUpperCase(Locale.ROOT);
        }

        if (descripcion != null) {
            descripcion = limpiarTexto(descripcion);
        }

        if (fechaRegistro == null) {
            fechaRegistro = LocalDate.now();
        }

        if (activo == null) {
            activo = true;
        }
    }

    public static String normalizarNombre(String value) {
        if (value == null) {
            return "";
        }

        String limpio = limpiarTexto(value);
        String sinAcentos = Normalizer.normalize(limpio, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "");

        return sinAcentos.toLowerCase(Locale.ROOT);
    }

    private static String limpiarTexto(String value) {
        return value.trim().replaceAll("\\s+", " ");
    }
}
