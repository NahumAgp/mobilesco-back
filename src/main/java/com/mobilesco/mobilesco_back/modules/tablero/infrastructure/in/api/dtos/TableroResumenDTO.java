package com.mobilesco.mobilesco_back.modules.tablero.infrastructure.in.api.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.mobilesco.mobilesco_back.modules.cotizacion.domain.models.EstadoCotizacion;
import com.mobilesco.mobilesco_back.modules.tablero.domain.PeriodoTablero;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TableroResumenDTO {
    private PeriodoTablero periodo;
    private LocalDate desde;
    private LocalDate hasta;
    private Indicadores indicadores;
    private List<CotizacionReciente> cotizacionesRecientes;
    private List<Alerta> alertas;

    @Data
    @Builder
    public static class Indicadores {
        private long cotizacionesActivas;
        private long cotizacionesPeriodo;
        private BigDecimal montoCotizado;
        private BigDecimal variacionMontoPorcentaje;
        private BigDecimal tasaCierrePorcentaje;
    }

    @Data
    @Builder
    public static class CotizacionReciente {
        private Long id;
        private String folio;
        private String cliente;
        private EstadoCotizacion estado;
        private BigDecimal total;
        private LocalDateTime fechaRegistro;
        private long partidas;
        private long unidades;
    }

    @Data
    @Builder
    public static class Alerta {
        private String tipo;
        private String severidad;
        private String titulo;
        private String descripcion;
        private long cantidad;
        private String ruta;
    }
}
