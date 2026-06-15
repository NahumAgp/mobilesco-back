package com.mobilesco.mobilesco_back.modules.producto.application.usecases;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mobilesco.mobilesco_back.modules.color.application.usecases.ColorUseCase;
import com.mobilesco.mobilesco_back.modules.color.infrastructure.in.api.dtos.ColorResponseDTO;
import com.mobilesco.mobilesco_back.modules.familia.application.usecases.FamiliaUseCase;
import com.mobilesco.mobilesco_back.modules.familia.infrastructure.in.api.dtos.FamiliaCreateDTO;
import com.mobilesco.mobilesco_back.modules.familia.infrastructure.in.api.dtos.FamiliaResponseDTO;
import com.mobilesco.mobilesco_back.modules.linea.application.usecases.LineaService;
import com.mobilesco.mobilesco_back.modules.linea.infrastructure.in.api.dtos.LineaResponseDTO;
import com.mobilesco.mobilesco_back.modules.material.application.usecases.MaterialUseCase;
import com.mobilesco.mobilesco_back.modules.material.infrastructure.in.api.dtos.MaterialResponseDTO;
import com.mobilesco.mobilesco_back.modules.modelo.application.usecases.ModeloService;
import com.mobilesco.mobilesco_back.modules.modelo.infrastructure.in.api.dtos.ModeloCreateDTO;
import com.mobilesco.mobilesco_back.modules.modelo.infrastructure.in.api.dtos.ModeloResponseDTO;
import com.mobilesco.mobilesco_back.modules.nivel.application.usecases.NivelService;
import com.mobilesco.mobilesco_back.modules.nivel.infrastructure.in.api.dtos.NivelCreateDTO;
import com.mobilesco.mobilesco_back.modules.nivel.infrastructure.in.api.dtos.NivelResponseDTO;
import com.mobilesco.mobilesco_back.modules.producto.infrastructure.in.api.dtos.ProductoCreateDTO;
import com.mobilesco.mobilesco_back.modules.producto.infrastructure.in.api.dtos.ProductoCreacionCompletaDTO;
import com.mobilesco.mobilesco_back.modules.producto.infrastructure.in.api.dtos.ProductoCreacionCompletaDTO.CategoriaBorradorDTO;
import com.mobilesco.mobilesco_back.modules.producto.infrastructure.in.api.dtos.ProductoCreacionCompletaDTO.VarianteBorradorDTO;
import com.mobilesco.mobilesco_back.modules.producto.infrastructure.in.api.dtos.ProductoCreacionCompletaResponseDTO;
import com.mobilesco.mobilesco_back.modules.producto.infrastructure.in.api.dtos.ProductoCreacionCompletaResponseDTO.ProductoCreadoDTO;
import com.mobilesco.mobilesco_back.modules.producto.infrastructure.in.api.dtos.ProductoResponseDTO;
import com.mobilesco.mobilesco_back.modules.shared.application.exceptions.BadRequestException;

@Service
public class ProductoCreacionCompletaService {

    private final LineaService lineaService;
    private final FamiliaUseCase familiaService;
    private final ModeloService modeloService;
    private final NivelService nivelService;
    private final MaterialUseCase materialService;
    private final ColorUseCase colorService;
    private final ProductoService productoService;

    public ProductoCreacionCompletaService(
            LineaService lineaService,
            FamiliaUseCase familiaService,
            ModeloService modeloService,
            NivelService nivelService,
            MaterialUseCase materialService,
            ColorUseCase colorService,
            ProductoService productoService) {
        this.lineaService = lineaService;
        this.familiaService = familiaService;
        this.modeloService = modeloService;
        this.nivelService = nivelService;
        this.materialService = materialService;
        this.colorService = colorService;
        this.productoService = productoService;
    }

    @Transactional
    public ProductoCreacionCompletaResponseDTO crear(ProductoCreacionCompletaDTO dto) {
        Map<String, Long> lineas = new HashMap<>();
        Map<String, Long> familias = new HashMap<>();
        Map<String, Long> modelos = new HashMap<>();
        Map<String, Long> categorias = new HashMap<>();
        Map<String, Long> materiales = new HashMap<>();
        Map<String, Long> colores = new HashMap<>();

        dto.getLineas().forEach(borrador -> {
            LineaResponseDTO creada = lineaService.crear(borrador);
            registrarRef(lineas, borrador.getRef(), creada.getId(), "linea");
        });

        dto.getFamilias().forEach(borrador -> {
            FamiliaCreateDTO payload = new FamiliaCreateDTO();
            payload.setCodigo(borrador.getCodigo());
            payload.setNombre(borrador.getNombre());
            payload.setDescripcion(borrador.getDescripcion());
            payload.setLineaId(resolverId(borrador.getLineaId(), borrador.getLineaRef(), lineas, "linea"));
            FamiliaResponseDTO creada = familiaService.crear(payload);
            registrarRef(familias, borrador.getRef(), creada.getId(), "familia");
        });

        ModeloResponseDTO modeloResultado = null;
        if (dto.getModelo() != null) {
            ModeloCreateDTO payload = new ModeloCreateDTO();
            payload.setCodigo(dto.getModelo().getCodigo());
            payload.setNombre(dto.getModelo().getNombre());
            payload.setDescripcion(dto.getModelo().getDescripcion());
            payload.setActivo(dto.getModelo().getActivo());
            payload.setFamiliaId(resolverId(
                    dto.getModelo().getFamiliaId(),
                    dto.getModelo().getFamiliaRef(),
                    familias,
                    "familia"));
            payload.setCategorias(new ArrayList<>(dto.getModelo().getCategorias()));
            modeloResultado = modeloService.crear(payload);
            registrarRef(modelos, dto.getModelo().getRef(), modeloResultado.getId(), "modelo");
            registrarCategoriasCreadas(dto.getModelo().getCategorias(), modeloResultado.getCategorias(), categorias);
        }

        for (CategoriaBorradorDTO borrador : dto.getCategorias()) {
            NivelCreateDTO payload = new NivelCreateDTO();
            payload.setCodigo(borrador.getCodigo());
            payload.setNombre(borrador.getNombre());
            payload.setDescripcion(borrador.getDescripcion());
            payload.setModeloId(borrador.getModeloId());
            NivelResponseDTO creada = nivelService.crear(payload);
            registrarRef(categorias, borrador.getRef(), creada.getId(), "categoria");
        }

        dto.getMateriales().forEach(borrador -> {
            MaterialResponseDTO creado = materialService.crear(borrador);
            registrarRef(materiales, borrador.getRef(), creado.getId(), "material");
        });

        dto.getColores().forEach(borrador -> {
            ColorResponseDTO creado = colorService.crear(borrador);
            registrarRef(colores, borrador.getRef(), creado.getId(), "color");
        });

        List<ProductoCreadoDTO> productos = new ArrayList<>();
        for (VarianteBorradorDTO borrador : dto.getVariantes()) {
            ProductoCreateDTO payload = new ProductoCreateDTO();
            payload.setNombre(borrador.getNombre());
            payload.setDescripcion(borrador.getDescripcion());
            payload.setDescripcionCorta(borrador.getDescripcionCorta());
            payload.setPesoVolumetrico(borrador.getPesoVolumetrico());
            payload.setAncho(borrador.getAncho());
            payload.setAlto(borrador.getAlto());
            payload.setFondo(borrador.getFondo());
            payload.setDimensiones(borrador.getDimensiones());
            payload.setPesoKg(borrador.getPesoKg());
            payload.setActivo(borrador.getActivo());
            payload.setModeloId(resolverId(borrador.getModeloId(), borrador.getModeloRef(), modelos, "modelo"));
            payload.setNivelId(resolverId(borrador.getCategoriaId(), borrador.getCategoriaRef(), categorias, "categoria"));
            payload.setMaterialId(resolverId(borrador.getMaterialId(), borrador.getMaterialRef(), materiales, "material"));
            payload.setColorId(resolverId(borrador.getColorId(), borrador.getColorRef(), colores, "color"));

            ProductoResponseDTO creado = productoService.crear(payload);
            productos.add(new ProductoCreadoDTO(borrador.getClientRef(), creado));
        }

        if (modeloResultado == null && !dto.getVariantes().isEmpty()) {
            Long modeloId = dto.getVariantes().get(0).getModeloId();
            if (modeloId != null) {
                modeloResultado = modeloService.obtenerPorId(modeloId);
            }
        }

        return new ProductoCreacionCompletaResponseDTO(modeloResultado, productos);
    }

    private void registrarCategoriasCreadas(
            List<CategoriaBorradorDTO> borradores,
            List<com.mobilesco.mobilesco_back.modules.modelo.infrastructure.in.api.dtos.ModeloCategoriaDTO> creadas,
            Map<String, Long> refs) {
        for (CategoriaBorradorDTO borrador : borradores) {
            creadas.stream()
                    .filter(item -> item.getNombre().equalsIgnoreCase(borrador.getNombre()))
                    .findFirst()
                    .ifPresent(item -> registrarRef(refs, borrador.getRef(), item.getId(), "categoria"));
        }
    }

    private Long resolverId(Long id, String ref, Map<String, Long> refs, String tipo) {
        if (id != null) return id;
        Long resuelto = ref == null ? null : refs.get(ref);
        if (resuelto == null) {
            throw new BadRequestException("No se pudo resolver la referencia de " + tipo + ": " + ref);
        }
        return resuelto;
    }

    private void registrarRef(Map<String, Long> refs, String ref, Long id, String tipo) {
        if (ref == null || ref.isBlank() || id == null) {
            throw new BadRequestException("No se pudo registrar la referencia de " + tipo);
        }
        if (refs.putIfAbsent(ref, id) != null) {
            throw new BadRequestException("Referencia temporal repetida: " + ref);
        }
    }
}
