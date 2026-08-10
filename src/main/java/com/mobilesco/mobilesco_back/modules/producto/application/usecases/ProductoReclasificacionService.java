package com.mobilesco.mobilesco_back.modules.producto.application.usecases;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mobilesco.mobilesco_back.modules.color.domain.models.ColorModel;
import com.mobilesco.mobilesco_back.modules.color.infrastructure.out.persistence.repositories.ColorRepository;
import com.mobilesco.mobilesco_back.modules.cotizacion.infrastructure.out.persistence.repositories.CotizacionRepository;
import com.mobilesco.mobilesco_back.modules.familia.domain.models.FamiliaModel;
import com.mobilesco.mobilesco_back.modules.familia.infrastructure.out.persistence.repositories.FamiliaRepository;
import com.mobilesco.mobilesco_back.modules.linea.domain.models.LineaModel;
import com.mobilesco.mobilesco_back.modules.linea.infrastructure.out.persistence.repositories.LineaRepository;
import com.mobilesco.mobilesco_back.modules.material.domain.models.MaterialModel;
import com.mobilesco.mobilesco_back.modules.material.infrastructure.out.persistence.repositories.MaterialRepository;
import com.mobilesco.mobilesco_back.modules.modelo.domain.models.ModeloModel;
import com.mobilesco.mobilesco_back.modules.modelo.infrastructure.out.persistence.repositories.ModeloRepository;
import com.mobilesco.mobilesco_back.modules.nivel.domain.models.NivelModel;
import com.mobilesco.mobilesco_back.modules.nivel.infrastructure.out.persistence.repositories.NivelRepository;
import com.mobilesco.mobilesco_back.modules.producto.domain.models.ProductoModel;
import com.mobilesco.mobilesco_back.modules.producto.infrastructure.in.api.dtos.ProductoReclasificacionRequestDTO;
import com.mobilesco.mobilesco_back.modules.producto.infrastructure.in.api.dtos.ProductoReclasificacionResponseDTO;
import com.mobilesco.mobilesco_back.modules.producto.infrastructure.in.api.dtos.ProductoReclasificacionResponseDTO.CambioSkuDTO;
import com.mobilesco.mobilesco_back.modules.producto.infrastructure.out.persistence.repositories.ProductoRepository;
import com.mobilesco.mobilesco_back.modules.shared.application.exceptions.ResourceNotFoundException;
import com.mobilesco.mobilesco_back.modules.shared.application.exceptions.ValidationException;
import com.mobilesco.mobilesco_back.modules.subfamilia.domain.models.SubfamiliaModel;
import com.mobilesco.mobilesco_back.modules.subfamilia.infrastructure.out.persistence.repositories.SubfamiliaRepository;

@Service
public class ProductoReclasificacionService {

    private final ProductoRepository productoRepository;
    private final ModeloRepository modeloRepository;
    private final LineaRepository lineaRepository;
    private final FamiliaRepository familiaRepository;
    private final SubfamiliaRepository subfamiliaRepository;
    private final NivelRepository nivelRepository;
    private final MaterialRepository materialRepository;
    private final ColorRepository colorRepository;
    private final CotizacionRepository cotizacionRepository;

    public ProductoReclasificacionService(
            ProductoRepository productoRepository,
            ModeloRepository modeloRepository,
            LineaRepository lineaRepository,
            FamiliaRepository familiaRepository,
            SubfamiliaRepository subfamiliaRepository,
            NivelRepository nivelRepository,
            MaterialRepository materialRepository,
            ColorRepository colorRepository,
            CotizacionRepository cotizacionRepository) {
        this.productoRepository = productoRepository;
        this.modeloRepository = modeloRepository;
        this.lineaRepository = lineaRepository;
        this.familiaRepository = familiaRepository;
        this.subfamiliaRepository = subfamiliaRepository;
        this.nivelRepository = nivelRepository;
        this.materialRepository = materialRepository;
        this.colorRepository = colorRepository;
        this.cotizacionRepository = cotizacionRepository;
    }

    @Transactional(readOnly = true)
    public ProductoReclasificacionResponseDTO previsualizar(
            Long productoId, ProductoReclasificacionRequestDTO request) {
        return construirRespuesta(preparar(productoId, request));
    }

    @Transactional
    public ProductoReclasificacionResponseDTO aplicar(
            Long productoId, ProductoReclasificacionRequestDTO request) {
        Contexto contexto = preparar(productoId, request);
        ProductoReclasificacionResponseDTO respuesta = construirRespuesta(contexto);
        if (!respuesta.isPermitido()) {
            throw new ValidationException(respuesta.getMotivoBloqueo());
        }

        contexto.modeloDestino().setFamilia(contexto.familia());
        contexto.modeloDestino().setSubfamilia(contexto.subfamilia());
        modeloRepository.save(contexto.modeloDestino());

        contexto.productoEditado().setModelo(contexto.modeloDestino());
        contexto.productoEditado().setNivel(contexto.nivel());
        contexto.productoEditado().setMaterial(contexto.material());
        contexto.productoEditado().setColor(contexto.color());

        for (int i = 0; i < contexto.productos().size(); i++) {
            contexto.productos().get(i).setSku(respuesta.getCambiosSku().get(i).getSkuNuevo());
        }
        productoRepository.saveAll(contexto.productos());
        return respuesta;
    }

    private Contexto preparar(Long productoId, ProductoReclasificacionRequestDTO request) {
        ProductoModel producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));
        ModeloModel modeloOrigen = producto.getModelo();
        if (modeloOrigen == null) {
            throw new ValidationException("El producto no tiene un modelo base que pueda reclasificarse.");
        }

        ModeloModel modeloDestino = modeloRepository.findById(request.getModeloId())
                .orElseThrow(() -> new ResourceNotFoundException("Modelo no encontrado"));
        NivelModel nivel = nivelRepository.findById(request.getNivelId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada"));
        if (nivel.getModelo() == null || !Objects.equals(nivel.getModelo().getId(), modeloDestino.getId())) {
            throw new ValidationException("La categoría seleccionada no pertenece al modelo indicado.");
        }
        MaterialModel material = materialRepository.findById(request.getMaterialId())
                .orElseThrow(() -> new ResourceNotFoundException("Material no encontrado"));
        ColorModel color = colorRepository.findById(request.getColorId())
                .orElseThrow(() -> new ResourceNotFoundException("Color no encontrado"));

        LineaModel linea = lineaRepository.findById(request.getLineaId())
                .orElseThrow(() -> new ResourceNotFoundException("Línea no encontrada"));
        FamiliaModel familia = familiaRepository.findById(request.getFamiliaId())
                .orElseThrow(() -> new ResourceNotFoundException("Familia no encontrada"));
        if (familia.getLinea() == null || !Objects.equals(familia.getLinea().getId(), linea.getId())) {
            throw new ValidationException(
                    "La familia seleccionada no pertenece a la línea indicada. Selecciona otra familia o créala en esa línea.");
        }

        SubfamiliaModel subfamilia = null;
        if (request.getSubfamiliaId() != null) {
            subfamilia = subfamiliaRepository.findById(request.getSubfamiliaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Subfamilia no encontrada"));
            if (subfamilia.getFamilia() == null
                    || !Objects.equals(subfamilia.getFamilia().getId(), familia.getId())) {
                throw new ValidationException(
                        "La subfamilia seleccionada no pertenece a la familia indicada. Selecciona otra subfamilia o créala en esa familia.");
            }
        }

        if (existeCodigoEnClasificacion(
                familia.getId(), subfamilia, modeloDestino.getCodigo(), modeloDestino.getId())) {
            throw new ValidationException(
                    "Ya existe otro modelo con el código " + modeloDestino.getCodigo() + " en la subfamilia seleccionada.");
        }
        if (existeNombreEnClasificacion(
                familia.getId(), subfamilia, modeloDestino.getNombre(), modeloDestino.getId())) {
            throw new ValidationException(
                    "Ya existe otro modelo con el nombre " + modeloDestino.getNombre() + " en la subfamilia seleccionada.");
        }

        List<ProductoModel> productos = new ArrayList<>(productoRepository.findByModeloId(modeloDestino.getId()));
        productos.removeIf(item -> Objects.equals(item.getId(), producto.getId()));
        productos.add(producto);
        return new Contexto(
                producto,
                modeloOrigen,
                modeloDestino,
                nivel,
                material,
                color,
                linea,
                familia,
                subfamilia,
                productos);
    }

    private ProductoReclasificacionResponseDTO construirRespuesta(Contexto contexto) {
        boolean cambioRuta = !Objects.equals(id(contexto.modeloDestino().getFamilia()), contexto.familia().getId())
                || !Objects.equals(id(contexto.modeloDestino().getSubfamilia()), id(contexto.subfamilia()));
        boolean cambioProducto = !Objects.equals(id(contexto.productoEditado().getModelo()), contexto.modeloDestino().getId())
                || !Objects.equals(id(contexto.productoEditado().getNivel()), contexto.nivel().getId())
                || !Objects.equals(id(contexto.productoEditado().getMaterial()), contexto.material().getId())
                || !Objects.equals(id(contexto.productoEditado().getColor()), contexto.color().getId());
        boolean cambio = cambioRuta || cambioProducto;

        List<CambioSkuDTO> cambios = new ArrayList<>();
        Set<String> nuevosSku = new HashSet<>();
        Set<Long> idsAfectados = new HashSet<>();
        contexto.productos().forEach(item -> idsAfectados.add(item.getId()));
        String motivo = null;

        for (ProductoModel producto : contexto.productos()) {
            boolean esProductoEditado = Objects.equals(producto.getId(), contexto.productoEditado().getId());
            String skuNuevo = generarSku(
                    contexto.linea(),
                    contexto.familia(),
                    contexto.subfamilia(),
                    contexto.modeloDestino(),
                    esProductoEditado ? contexto.nivel() : producto.getNivel(),
                    esProductoEditado ? contexto.material() : producto.getMaterial(),
                    esProductoEditado ? contexto.color() : producto.getColor());
            cambios.add(CambioSkuDTO.builder()
                    .productoId(producto.getId())
                    .nombre(producto.getNombre())
                    .skuAnterior(producto.getSku())
                    .skuNuevo(skuNuevo)
                    .build());

            if (!nuevosSku.add(skuNuevo.toLowerCase(Locale.ROOT))) {
                motivo = "Los cambios producirían SKUs duplicados.";
            }
            ProductoModel ocupante = productoRepository.findBySkuIgnoreCase(skuNuevo).orElse(null);
            if (ocupante != null && !idsAfectados.contains(ocupante.getId())) {
                motivo = "Ya existe otro producto con el SKU " + skuNuevo + ".";
            }
        }

        List<Long> ids = contexto.productos().stream().map(ProductoModel::getId).toList();
        if (cambio && !ids.isEmpty() && cotizacionRepository.existsByProductoIdsInCotizaciones(ids)) {
            motivo = "Una o más variantes afectadas ya aparecen en cotizaciones y no pueden modificar su SKU.";
        }

        long variantesAfectadas = cambios.stream()
                .filter(item -> !item.getSkuAnterior().equalsIgnoreCase(item.getSkuNuevo()))
                .count();
        return ProductoReclasificacionResponseDTO.builder()
                .productoBaseId(contexto.modeloDestino().getId())
                .productoBaseNombre(contexto.modeloDestino().getNombre())
                .rutaActual(ruta(
                        contexto.modeloOrigen().getFamilia(),
                        contexto.modeloOrigen().getSubfamilia(),
                        contexto.modeloOrigen()))
                .rutaDestino(ruta(contexto.familia(), contexto.subfamilia(), contexto.modeloDestino()))
                .variantesAfectadas((int) variantesAfectadas)
                .permitido(motivo == null)
                .motivoBloqueo(motivo)
                .cambiosSku(cambios)
                .build();
    }

    private String generarSku(
            LineaModel linea,
            FamiliaModel familia,
            SubfamiliaModel subfamilia,
            ModeloModel modelo,
            NivelModel nivel,
            MaterialModel material,
            ColorModel color) {
        if (nivel == null || material == null || color == null) {
            throw new ValidationException(
                    "Una variante del modelo no tiene categoría, material o color y no puede regenerar su SKU.");
        }
        String codigoSubfamilia = subfamilia != null ? codigo(subfamilia.getCodigo(), "subfamilia") : "";
        return (codigo(linea.getCodigo(), "línea")
                + codigo(familia.getCodigo(), "familia")
                + codigoSubfamilia
                + codigo(modelo.getCodigo(), "modelo")
                + "-" + codigo(nivel.getCodigo(), "categoría")
                + "-" + codigo(material.getCodigo(), "material")
                + "-" + codigo(color.getCodigo(), "color")).toUpperCase(Locale.ROOT);
    }

    private String ruta(FamiliaModel familia, SubfamiliaModel subfamilia, ModeloModel modelo) {
        if (familia == null || familia.getLinea() == null) return "Sin clasificación";
        return familia.getLinea().getNombre() + " / " + familia.getNombre() + " / "
                + (subfamilia != null ? subfamilia.getNombre() : "Sin subfamilia")
                + " / " + modelo.getNombre();
    }

    private Long id(Object item) {
        if (item instanceof FamiliaModel familia) return familia.getId();
        if (item instanceof SubfamiliaModel subfamilia) return subfamilia.getId();
        if (item instanceof ModeloModel modelo) return modelo.getId();
        if (item instanceof NivelModel nivel) return nivel.getId();
        if (item instanceof MaterialModel material) return material.getId();
        if (item instanceof ColorModel color) return color.getId();
        return null;
    }

    private String codigo(String valor, String catalogo) {
        if (valor == null || valor.isBlank()) {
            throw new ValidationException("Falta el código de " + catalogo + " para regenerar el SKU.");
        }
        return valor.trim();
    }

    private boolean existeCodigoEnClasificacion(
            Long familiaId, SubfamiliaModel subfamilia, String codigo, Long modeloId) {
        if (subfamilia != null) {
            return modeloRepository.existsBySubfamiliaIdAndCodigoIgnoreCaseAndIdNot(
                    subfamilia.getId(), codigo, modeloId);
        }
        return modeloRepository.existsByFamiliaIdAndSubfamiliaIsNullAndCodigoIgnoreCaseAndIdNot(
                familiaId, codigo, modeloId);
    }

    private boolean existeNombreEnClasificacion(
            Long familiaId, SubfamiliaModel subfamilia, String nombre, Long modeloId) {
        if (subfamilia != null) {
            return modeloRepository.existsBySubfamiliaIdAndNombreIgnoreCaseAndIdNot(
                    subfamilia.getId(), nombre, modeloId);
        }
        return modeloRepository.existsByFamiliaIdAndSubfamiliaIsNullAndNombreIgnoreCaseAndIdNot(
                familiaId, nombre, modeloId);
    }

    private record Contexto(
            ProductoModel productoEditado,
            ModeloModel modeloOrigen,
            ModeloModel modeloDestino,
            NivelModel nivel,
            MaterialModel material,
            ColorModel color,
            LineaModel linea,
            FamiliaModel familia,
            SubfamiliaModel subfamilia,
            List<ProductoModel> productos) {
    }
}
