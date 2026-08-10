package com.mobilesco.mobilesco_back.modules.ordenproduccion.infrastructure.in.api.dtos;

import java.math.BigDecimal;
import java.time.*;
import java.util.List;
import lombok.*;

@Data @Builder
public class OrdenProduccionResponseDTO {
    private Long id; private String folio; private String origen; private String estado;
    private Long cotizacionId; private String cotizacionFolio; private Long clienteId; private String clienteNombre;
    private LocalDate fechaInicioProgramada; private LocalDate fechaCompromiso;
    private String observaciones; private String motivoCancelacion; private String creadoPor; private String actualizadoPor;
    private LocalDateTime fechaRegistro; private LocalDateTime fechaActualizacion;
    private BigDecimal porcentajeAvance; private boolean tieneFaltantes;
    private List<Partida> partidas; private List<Insumo> insumos; private List<Operacion> operaciones; private List<Avance> avances;

    @Data @Builder public static class Partida { private Long id; private Long productoId; private String sku; private String nombre; private BigDecimal cantidadPlaneada; private BigDecimal cantidadTerminada; private BigDecimal porcentajeAvance; }
    @Data @Builder public static class Insumo { private Long id; private Long insumoId; private String codigo; private String nombre; private String unidad; private BigDecimal requerido; private BigDecimal surtido; private BigDecimal pendiente; private BigDecimal apartado; private BigDecimal porApartar; private BigDecimal existencia; private BigDecimal disponibleGeneral; private BigDecimal disponibleParaOrden; private boolean faltante; }
    @Data @Builder public static class Operacion { private Long id; private Long partidaId; private String producto; private Long operacionId; private String codigo; private String nombre; private String centroTrabajo; private Integer secuencia; private Integer repeticionesPlaneadas; private BigDecimal tiempoPlaneado; private String estado; private LocalDateTime fechaInicio; private LocalDateTime fechaFin; }
    @Data @Builder public static class Avance { private Long id; private Long partidaId; private String producto; private BigDecimal cantidad; private String observaciones; private String usuario; private LocalDateTime fechaRegistro; }
}
