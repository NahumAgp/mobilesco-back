package com.mobilesco.mobilesco_back.modules.producto.infrastructure.in.api.dtos;

import java.util.ArrayList;
import java.util.List;

import com.mobilesco.mobilesco_back.modules.color.infrastructure.in.api.dtos.ColorCreateDTO;
import com.mobilesco.mobilesco_back.modules.linea.infrastructure.in.api.dtos.LineaCreateDTO;
import com.mobilesco.mobilesco_back.modules.material.infrastructure.in.api.dtos.MaterialCreateDTO;
import com.mobilesco.mobilesco_back.modules.modelo.infrastructure.in.api.dtos.ModeloCategoriaDTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
public class ProductoCreacionCompletaDTO {

    @Valid
    private List<LineaBorradorDTO> lineas = new ArrayList<>();

    @Valid
    private List<FamiliaBorradorDTO> familias = new ArrayList<>();

    @Valid
    private ModeloBorradorDTO modelo;

    @Valid
    private List<CategoriaBorradorDTO> categorias = new ArrayList<>();

    @Valid
    private List<MaterialBorradorDTO> materiales = new ArrayList<>();

    @Valid
    private List<ColorBorradorDTO> colores = new ArrayList<>();

    @Valid
    @NotEmpty(message = "Debe incluir al menos un producto")
    private List<VarianteBorradorDTO> variantes = new ArrayList<>();

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class LineaBorradorDTO extends LineaCreateDTO {
        @NotBlank(message = "La referencia temporal de la linea es obligatoria")
        private String ref;
    }

    @Data
    public static class FamiliaBorradorDTO {
        @NotBlank(message = "La referencia temporal de la familia es obligatoria")
        private String ref;
        @NotBlank(message = "El nombre de la familia es obligatorio")
        private String nombre;
        private String codigo;
        private String descripcion;
        private Long lineaId;
        private String lineaRef;
    }

    @Data
    public static class ModeloBorradorDTO {
        @NotBlank(message = "La referencia temporal del modelo es obligatoria")
        private String ref;
        @NotBlank(message = "El nombre del modelo es obligatorio")
        private String nombre;
        private String codigo;
        private String descripcion;
        private Boolean activo = true;
        private Long familiaId;
        private String familiaRef;
        @Valid
        @NotEmpty(message = "El modelo debe tener al menos una categoria")
        private List<CategoriaBorradorDTO> categorias = new ArrayList<>();
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class CategoriaBorradorDTO extends ModeloCategoriaDTO {
        @NotBlank(message = "La referencia temporal de la categoria es obligatoria")
        private String ref;
        private Long modeloId;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class MaterialBorradorDTO extends MaterialCreateDTO {
        @NotBlank(message = "La referencia temporal del material es obligatoria")
        private String ref;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class ColorBorradorDTO extends ColorCreateDTO {
        @NotBlank(message = "La referencia temporal del color es obligatoria")
        private String ref;
    }

    @Data
    public static class VarianteBorradorDTO {
        @NotBlank(message = "La referencia del producto es obligatoria")
        private String clientRef;
        @NotBlank(message = "El nombre del producto es obligatorio")
        private String nombre;
        private String descripcion;
        private String descripcionCorta;
        private Double pesoVolumetrico;
        private Double ancho;
        private Double alto;
        private Double fondo;
        private Boolean activo = true;
        private Long modeloId;
        private String modeloRef;
        private Long categoriaId;
        private String categoriaRef;
        private Long materialId;
        private String materialRef;
        private Long colorId;
        private String colorRef;
    }
}
