package com.mobilesco.mobilesco_back.modules.ordenproduccion.infrastructure.in.api.dtos;

import java.math.BigDecimal;
import java.util.List;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

public final class OrdenProduccionAccionesDTO {
    private OrdenProduccionAccionesDTO() {}
    @Data public static class Conversion { private java.time.LocalDate fechaInicioProgramada; private java.time.LocalDate fechaCompromiso; @Size(max=1000) private String observaciones; }
    @Data public static class Cancelacion { @NotBlank @Size(max=1000) private String motivo; }
    @Data public static class Surtido { @NotEmpty @Valid private List<Insumo> insumos; @Size(max=500) private String observaciones; }
    @Data public static class Insumo { @NotNull private Long insumoId; @NotNull @DecimalMin(value="0.0001") private BigDecimal cantidad; }
    @Data public static class CambioOperacion { @NotBlank private String estado; }
    @Data public static class Avance { @NotNull @DecimalMin(value="0.001") private BigDecimal cantidad; @Size(max=500) private String observaciones; }
}
