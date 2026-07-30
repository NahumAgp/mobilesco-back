package com.mobilesco.mobilesco_back.modules.producto.application.usecases;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mobilesco.mobilesco_back.modules.cotizacion.infrastructure.out.persistence.repositories.CotizacionRepository;
import com.mobilesco.mobilesco_back.modules.familia.domain.models.FamiliaModel;
import com.mobilesco.mobilesco_back.modules.familia.infrastructure.out.persistence.repositories.FamiliaRepository;
import com.mobilesco.mobilesco_back.modules.linea.domain.models.LineaModel;
import com.mobilesco.mobilesco_back.modules.linea.infrastructure.out.persistence.repositories.LineaRepository;
import com.mobilesco.mobilesco_back.modules.modelo.domain.models.ModeloModel;
import com.mobilesco.mobilesco_back.modules.modelo.infrastructure.out.persistence.repositories.ModeloRepository;
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
    private final CotizacionRepository cotizacionRepository;

    public ProductoReclasificacionService(
            ProductoRepository productoRepository,
            ModeloRepository modeloRepository,
            LineaRepository lineaRepository,
            FamiliaRepository familiaRepository,
            SubfamiliaRepository subfamiliaRepository,
            CotizacionRepository cotizacionRepository) {
        this.productoRepository = productoRepository;
        this.modeloRepository = modeloRepository;
        this.lineaRepository = lineaRepository;
        this.familiaRepository = familiaRepository;
        this.subfamiliaRepository = subfamiliaRepository;
        this.cotizacionRepository = cotizacionRepository;
    }

    @Transactional(readOnly = true)
    public ProductoReclasificacionResponseDTO previsualizar(
            Long productoId, ProductoReclasificacionRequestDTO request) {
        Contexto contexto = preparar(productoId, request);
        return construirRespuesta(contexto);
    }

    @Transactional
    public ProductoReclasificacionResponseDTO aplicar(
            Long productoId, ProductoReclasificacionRequestDTO request) {
        Contexto contexto = preparar(productoId, request);
        ProductoReclasificacionResponseDTO respuesta = construirRespuesta(contexto);
        if (!respuesta.isPermitido()) {
            throw new ValidationException(respuesta.getMotivoBloqueo());
        }

        contexto.modelo().setFamilia(contexto.familia());
        contexto.modelo().setSubfamilia(contexto.subfamilia());
        modeloRepository.save(contexto.modelo());

        for (int i = 0; i < contexto.productos().size(); i++) {
            contexto.productos().get(i).setSku(respuesta.getCambiosSku().get(i).getSkuNuevo());
        }
        productoRepository.saveAll(contexto.productos());
        return respuesta;
    }

    private Contexto preparar(Long productoId, ProductoReclasificacionRequestDTO request) {
        ProductoModel producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));
        ModeloModel modelo = producto.getModelo();
        if (modelo == null) {
            throw new ValidationException("El producto no tiene un modelo base que pueda reclasificarse.");
        }

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

        if (modeloRepository.existsByFamiliaIdAndCodigoIgnoreCaseAndIdNot(
                familia.getId(), modelo.getCodigo(), modelo.getId())) {
            throw new ValidationException(
                    "Ya existe otro modelo con el código " + modelo.getCodigo() + " en la familia seleccionada.");
        }
        if (modeloRepository.existsByFamiliaIdAndNombreIgnoreCaseAndIdNot(
                familia.getId(), modelo.getNombre(), modelo.getId())) {
            throw new ValidationException(
                    "Ya existe otro modelo con el nombre " + modelo.getNombre() + " en la familia seleccionada.");
        }

        List<ProductoModel> productos = productoRepository.findByModeloId(modelo.getId());
        return new Contexto(modelo, linea, familia, subfamilia, productos);
    }

    private ProductoReclasificacionResponseDTO construirRespuesta(Contexto contexto) {
        boolean cambio = !Objects.equals(id(contexto.modelo().getFamilia()), contexto.familia().getId())
                || !Objects.equals(id(contexto.modelo().getSubfamilia()), id(contexto.subfamilia()));
        List<CambioSkuDTO> cambios = new ArrayList<>();
        Set<String> nuevosSku = new HashSet<>();
        String motivo = null;

        for (ProductoModel producto : contexto.productos()) {
            String skuNuevo = generarSku(contexto.linea(), contexto.familia(), contexto.subfamilia(),
                    contexto.modelo(), producto);
            cambios.add(CambioSkuDTO.builder()
                    .productoId(producto.getId())
                    .nombre(producto.getNombre())
                    .skuAnterior(producto.getSku())
                    .skuNuevo(skuNuevo)
                    .build());

            if (!nuevosSku.add(skuNuevo.toLowerCase(Locale.ROOT))) {
                motivo = "La nueva clasificación produciría SKUs duplicados.";
            }
            ProductoModel ocupante = productoRepository.findBySkuIgnoreCase(skuNuevo).orElse(null);
            if (ocupante != null && !Objects.equals(ocupante.getModelo().getId(), contexto.modelo().getId())) {
                motivo = "Ya existe otro producto con el SKU " + skuNuevo + ".";
            }
        }

        List<Long> ids = contexto.productos().stream().map(ProductoModel::getId).toList();
        if (cambio && !ids.isEmpty() && cotizacionRepository.existsByProductoIdsInCotizaciones(ids)) {
            motivo = "Una o más variantes del modelo ya aparecen en cotizaciones y no pueden reclasificarse.";
        }

        return ProductoReclasificacionResponseDTO.builder()
                .productoBaseId(contexto.modelo().getId())
                .productoBaseNombre(contexto.modelo().getNombre())
                .rutaActual(ruta(contexto.modelo().getFamilia(), contexto.modelo().getSubfamilia(), contexto.modelo()))
                .rutaDestino(ruta(contexto.familia(), contexto.subfamilia(), contexto.modelo()))
                .variantesAfectadas(cambio ? contexto.productos().size() : 0)
                .permitido(motivo == null)
                .motivoBloqueo(motivo)
                .cambiosSku(cambios)
                .build();
    }

    private String generarSku(LineaModel linea, FamiliaModel familia, SubfamiliaModel subfamilia,
            ModeloModel modelo, ProductoModel producto) {
        if (producto.getNivel() == null || producto.getMaterial() == null || producto.getColor() == null) {
            throw new ValidationException(
                    "Una variante del modelo no tiene categoría, material o color y no puede regenerar su SKU.");
        }
        String codigoSubfamilia = subfamilia != null ? codigo(subfamilia.getCodigo(), "subfamilia") : "";
        return (codigo(linea.getCodigo(), "línea")
                + codigo(familia.getCodigo(), "familia")
                + codigoSubfamilia
                + codigo(modelo.getCodigo(), "modelo")
                + "-" + codigo(producto.getNivel().getCodigo(), "categoría")
                + "-" + codigo(producto.getMaterial().getCodigo(), "material")
                + "-" + codigo(producto.getColor().getCodigo(), "color")).toUpperCase(Locale.ROOT);
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
        return null;
    }

    private String codigo(String valor, String catalogo) {
        if (valor == null || valor.isBlank()) {
            throw new ValidationException("Falta el código de " + catalogo + " para regenerar el SKU.");
        }
        return valor.trim();
    }

    private record Contexto(
            ModeloModel modelo,
            LineaModel linea,
            FamiliaModel familia,
            SubfamiliaModel subfamilia,
            List<ProductoModel> productos) {
    }
}
