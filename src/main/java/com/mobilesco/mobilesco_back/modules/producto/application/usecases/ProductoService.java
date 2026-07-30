/*
 * PATH (direccion): mobilesco-back/src/main/java/com/mobilesco/mobilesco_back/modules/producto/application/usecases/ProductoService.java
 * AUTOR: Nahum Aguilar
 * NOMBRE DE LA CLASE: ProductoService
 * CONTEXTO: Servicio de aplicacion del modulo Producto.
 * NOTAS: Gestiona catalogo de productos, estructura de costos y reporte.
 */
package com.mobilesco.mobilesco_back.modules.producto.application.usecases;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mobilesco.mobilesco_back.dto.common.PageResponseDTO;
import com.mobilesco.mobilesco_back.modules.imagen.infrastructure.in.api.dtos.ImagenResponseDTO;
import com.mobilesco.mobilesco_back.modules.costoindirecto.application.usecases.CostoIndirectoService;
import com.mobilesco.mobilesco_back.modules.costoindirecto.infrastructure.in.api.dtos.CifResumenDTO;
import com.mobilesco.mobilesco_back.modules.insumo.domain.models.InsumoModel;
import com.mobilesco.mobilesco_back.modules.producto.infrastructure.in.api.dtos.ProductoCostoIndirectoDTO;
import com.mobilesco.mobilesco_back.modules.producto.infrastructure.in.api.dtos.ProductoOperacionResponseDTO;
import com.mobilesco.mobilesco_back.modules.shared.application.exceptions.ResourceNotFoundException;
import com.mobilesco.mobilesco_back.modules.shared.application.exceptions.ValidationException;
import com.mobilesco.mobilesco_back.modules.color.domain.models.ColorModel;
import com.mobilesco.mobilesco_back.modules.familia.domain.models.FamiliaModel;
import com.mobilesco.mobilesco_back.modules.linea.domain.models.LineaModel;
import com.mobilesco.mobilesco_back.modules.material.domain.models.MaterialModel;
import com.mobilesco.mobilesco_back.modules.nivel.domain.models.NivelModel;
import com.mobilesco.mobilesco_back.modules.producto.domain.models.ProductoInsumoModel;
import com.mobilesco.mobilesco_back.modules.color.infrastructure.out.persistence.repositories.ColorRepository;
import com.mobilesco.mobilesco_back.modules.cotizacion.infrastructure.out.persistence.repositories.CotizacionRepository;
import com.mobilesco.mobilesco_back.modules.material.infrastructure.out.persistence.repositories.MaterialRepository;
import com.mobilesco.mobilesco_back.modules.modelo.domain.models.ModeloModel;
import com.mobilesco.mobilesco_back.modules.modelo.infrastructure.out.persistence.repositories.ModeloRepository;
import com.mobilesco.mobilesco_back.modules.producto.domain.models.ProductoModel;
import com.mobilesco.mobilesco_back.modules.producto.infrastructure.in.api.dtos.CatalogoFacetasDTO;
import com.mobilesco.mobilesco_back.modules.producto.infrastructure.in.api.dtos.ModeloCatalogoPublicoDTO;
import com.mobilesco.mobilesco_back.modules.producto.infrastructure.in.api.dtos.ProductoCreateDTO;
import com.mobilesco.mobilesco_back.modules.producto.infrastructure.in.api.dtos.ProductoEstructuraCostosDTO;
import com.mobilesco.mobilesco_back.modules.producto.infrastructure.in.api.dtos.ProductoFichaDTO;
import com.mobilesco.mobilesco_back.modules.producto.infrastructure.in.api.dtos.ProductoInsumoResponseDTO;
import com.mobilesco.mobilesco_back.modules.producto.infrastructure.in.api.dtos.ProductoResponseDTO;
import com.mobilesco.mobilesco_back.modules.producto.infrastructure.in.api.dtos.ProductoUpdateDTO;
import com.mobilesco.mobilesco_back.modules.producto.infrastructure.out.persistence.repositories.ProductoRepository;
import com.mobilesco.mobilesco_back.modules.shared.infrastructure.excel.ExcelReportBuilder;
import com.mobilesco.mobilesco_back.modules.shared.infrastructure.sort.TypeSafeSorts;
import com.mobilesco.mobilesco_back.modules.nivel.infrastructure.out.persistence.repositories.NivelRepository;
import com.mobilesco.mobilesco_back.modules.producto.infrastructure.out.persistence.repositories.ProductoInsumoRepository;
import com.mobilesco.mobilesco_back.modules.producto.infrastructure.out.persistence.repositories.ProductoOperacionRepository;
import com.mobilesco.mobilesco_back.modules.imagen.application.usecases.ImagenService;

@Service
public class ProductoService {

    private final ProductoRepository productoRepository;
    
    private final ModeloRepository modeloRepository;
    private final NivelRepository nivelRepository;
    private final ColorRepository colorRepository;
    private final MaterialRepository materialRepository;
    private final ProductoInsumoRepository productoInsumoRepository;
    private final ProductoOperacionRepository productoOperacionRepository;
    private final ImagenService imagenService;
    private final CostoIndirectoService costoIndirectoService;
    private final ProductoPlantillaModeloService productoPlantillaModeloService;
    private final CotizacionRepository cotizacionRepository;

    public ProductoService(
            ProductoRepository productoRepository,
            ModeloRepository modeloRepository,
            NivelRepository nivelRepository,
            ColorRepository colorRepository,
            MaterialRepository materialRepository,
            ProductoInsumoRepository productoInsumoRepository,
            ProductoOperacionRepository productoOperacionRepository,
            ImagenService imagenService,
            CostoIndirectoService costoIndirectoService,
            ProductoPlantillaModeloService productoPlantillaModeloService,
            CotizacionRepository cotizacionRepository) {
        this.productoRepository = productoRepository;
        this.modeloRepository = modeloRepository;
        this.nivelRepository = nivelRepository;
        this.colorRepository = colorRepository;
        this.materialRepository = materialRepository;
        this.productoInsumoRepository = productoInsumoRepository;
        this.productoOperacionRepository = productoOperacionRepository;
        this.imagenService = imagenService;
        this.costoIndirectoService = costoIndirectoService;
        this.productoPlantillaModeloService = productoPlantillaModeloService;
        this.cotizacionRepository = cotizacionRepository;
    }

    @Transactional
    public ProductoResponseDTO crear(ProductoCreateDTO dto) {
        ModeloModel modelo = modeloRepository.findById(dto.getModeloId())
                .orElseThrow(() -> new ResourceNotFoundException("Modelo no encontrado"));

        NivelModel nivel = nivelRepository.findById(dto.getNivelId())
                .orElseThrow(() -> new ResourceNotFoundException("Nivel no encontrado"));
        validarNivelDelModelo(modelo, nivel);

        ColorModel color = colorRepository.findById(dto.getColorId())
                .orElseThrow(() -> new ResourceNotFoundException("Color no encontrado"));

        MaterialModel material = materialRepository.findById(dto.getMaterialId())
                .orElseThrow(() -> new ResourceNotFoundException("Material no encontrado"));

        String skuGenerado = generarSkuProducto(modelo, nivel, material, color);
        if (productoRepository.existsBySkuIgnoreCase(skuGenerado)) {
            throw new ValidationException("Ya existe un producto con SKU: " + skuGenerado);
        }

        ProductoModel producto = ProductoModel.builder()
                .sku(skuGenerado)
                .nombre(dto.getNombre())
                .descripcion(dto.getDescripcion())
                .descripcionCorta(dto.getDescripcionCorta())
                .pesoVolumetrico(dto.getPesoVolumetrico())
                .ancho(dto.getAncho())
                .alto(dto.getAlto())
                .fondo(dto.getFondo())
                .dimensiones(dto.getDimensiones())
                .pesoKg(dto.getPesoKg())
                .modelo(modelo)
                .nivel(nivel)
                .material(material)
                .color(color)
                .activo(dto.getActivo() == null || Boolean.TRUE.equals(dto.getActivo()))
                .build();

        ProductoModel saved = productoRepository.save(producto);
        productoPlantillaModeloService.aplicarAProducto(saved);
        return mapToResponseDTO(saved);
    }

    @Transactional(readOnly = true)
    public byte[] generarReporteExcel(Boolean activo, String busqueda, String sortBy, String direction) {
        List<ProductoModel> productos = productoRepository.findAll(construirSortProductos(sortBy, direction));

        List<ProductoModel> filtrados = productos.stream()
                .filter(producto -> activo == null || Objects.equals(producto.getActivo(), activo))
                .filter(producto -> coincideBusqueda(producto, busqueda))
                .collect(Collectors.toList());

        String[] headers = {
                "ID", "SKU", "Nombre", "Descripcion", "Descripcion corta", "Peso volumetrico", "Ancho", "Alto", "Fondo", "Modelo", "Familia", "Linea", "Nivel", "Material", "Color", "Estado", "Creado", "Actualizado"
        };

        return ExcelReportBuilder.generate(
                "Productos",
                "Reporte de productos",
                headers,
                filtrados.stream()
                        .map(producto -> new Object[] {
                                producto.getId() != null ? producto.getId() : 0L,
                                nvl(producto.getSku()),
                                nvl(producto.getNombre()),
                                nvl(producto.getDescripcion()),
                                nvl(producto.getDescripcionCorta()),
                                producto.getPesoVolumetrico() != null ? producto.getPesoVolumetrico() : "",
                                producto.getAncho() != null ? producto.getAncho() : "",
                                producto.getAlto() != null ? producto.getAlto() : "",
                                producto.getFondo() != null ? producto.getFondo() : "",
                                producto.getModelo() != null ? nvl(producto.getModelo().getNombre()) : "",
                                producto.getModelo() != null && producto.getModelo().getFamilia() != null
                                        ? nvl(producto.getModelo().getFamilia().getNombre()) : "",
                                producto.getModelo() != null
                                        && producto.getModelo().getFamilia() != null
                                        && producto.getModelo().getFamilia().getLinea() != null
                                        ? nvl(producto.getModelo().getFamilia().getLinea().getNombre()) : "",
                                producto.getNivel() != null ? nvl(producto.getNivel().getNombre()) : "",
                                producto.getMaterial() != null ? nvl(producto.getMaterial().getNombre()) : "",
                                producto.getColor() != null ? nvl(producto.getColor().getNombre()) : "",
                                Boolean.TRUE.equals(producto.getActivo()) ? "Activo" : "Inactivo",
                                producto.getCreatedAt() != null ? producto.getCreatedAt().toString() : "",
                                producto.getUpdatedAt() != null ? producto.getUpdatedAt().toString() : ""
                        })
                        .collect(Collectors.toList()));
    }

    @Transactional
    public ProductoResponseDTO actualizar(Long id, ProductoUpdateDTO dto) {
        ProductoModel producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));

        ModeloModel modelo = dto.getModeloId() != null
                ? modeloRepository.findById(dto.getModeloId())
                        .orElseThrow(() -> new ResourceNotFoundException("Modelo no encontrado"))
                : null;
        NivelModel nivel = dto.getNivelId() != null
                ? nivelRepository.findById(dto.getNivelId())
                        .orElseThrow(() -> new ResourceNotFoundException("Nivel no encontrado"))
                : null;
        validarNivelDelModelo(modelo, nivel);

        ColorModel color = null;
        if (dto.getColorId() != null) {
            color = colorRepository.findById(dto.getColorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Color no encontrado"));
        }

        MaterialModel material = null;
        if (dto.getMaterialId() != null) {
            material = materialRepository.findById(dto.getMaterialId())
                    .orElseThrow(() -> new ResourceNotFoundException("Material no encontrado"));
        }
        if (modelo == null || nivel == null || material == null || color == null) {
            throw new ValidationException(
                    "Modelo, categoría, material y color son obligatorios para calcular el SKU.");
        }

        boolean cambioClasificacion = !Objects.equals(id(producto.getModelo()), id(modelo))
                || !Objects.equals(id(producto.getNivel()), id(nivel))
                || !Objects.equals(id(producto.getMaterial()), id(material))
                || !Objects.equals(id(producto.getColor()), id(color));
        if (cambioClasificacion
                && cotizacionRepository.existsByProductoIdsInCotizaciones(List.of(producto.getId()))) {
            throw new ValidationException(
                    "No se puede cambiar la clasificación porque el producto ya aparece en cotizaciones.");
        }

        String skuGenerado = generarSkuProducto(modelo, nivel, material, color);
        if (!producto.getSku().equalsIgnoreCase(skuGenerado)
                && productoRepository.existsBySkuIgnoreCase(skuGenerado)) {
            throw new ValidationException("Ya existe un producto con SKU: " + skuGenerado);
        }

        producto.setModelo(modelo);
        producto.setNivel(nivel);
        producto.setColor(color);
        producto.setMaterial(material);
        producto.setSku(skuGenerado);
        producto.setNombre(dto.getNombre());
        producto.setDescripcion(dto.getDescripcion());
        producto.setDescripcionCorta(dto.getDescripcionCorta());
        producto.setPesoVolumetrico(dto.getPesoVolumetrico());
        producto.setAncho(dto.getAncho());
        producto.setAlto(dto.getAlto());
        producto.setFondo(dto.getFondo());
        producto.setDimensiones(dto.getDimensiones());
        producto.setPesoKg(dto.getPesoKg());
        if (dto.getActivo() != null) {
            producto.setActivo(dto.getActivo());
        }

        ProductoModel updated = productoRepository.save(producto);
        return mapToResponseDTO(updated);
    }

    @Transactional(readOnly = true)
    public ProductoResponseDTO obtenerPorId(Long id) {
        ProductoModel producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));
        return mapToResponseDTO(producto);
    }

    @Transactional(readOnly = true)
    public ProductoResponseDTO obtenerPorSku(String sku) {
        ProductoModel producto = productoRepository.findBySku(sku)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con SKU: " + sku));
        return mapToResponseDTO(producto);
    }

    @Transactional(readOnly = true)
    public ProductoEstructuraCostosDTO obtenerEstructuraCostos(Long productoId) {
        ProductoModel producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));

        List<ProductoInsumoModel> productoInsumos = productoInsumoRepository.findByProductoId(productoId);
        validarCostosCotizacion(productoInsumos);

        List<ProductoInsumoResponseDTO> insumos = productoInsumos
                .stream()
                .map(pi -> {
                    double costoUnitarioSeguro = obtenerCostoCotizacionValido(pi.getInsumo());
                    double cantidad = nz(pi.getCantidad());
                    double desperdicio = nz(pi.getDesperdicioPorcentaje());
                    double cantidadConDesperdicio = cantidad * (1 + desperdicio / 100);
                    double subtotal = cantidadConDesperdicio * costoUnitarioSeguro;

                    return ProductoInsumoResponseDTO.builder()
                            .id(pi.getId())
                            .productoId(productoId)
                            .insumoId(pi.getInsumo().getId())
                            .insumoNombre(pi.getInsumo().getNombre())
                            .insumoUnidad(pi.getInsumo().getUnidadMedida().getSimbolo())
                            .cantidad(pi.getCantidad())
                            .desperdicioPorcentaje(pi.getDesperdicioPorcentaje())
                            .cantidadConDesperdicio(cantidadConDesperdicio)
                            .observaciones(pi.getObservaciones())
                            .costoUnitario(costoUnitarioSeguro)
                            .subtotal(subtotal)
                            .fechaRegistro(pi.getFechaRegistro())
                            .fechaActualizacion(pi.getFechaActualizacion())
                            .build();
                })
                .collect(Collectors.toList());

        List<ProductoOperacionResponseDTO> operaciones = productoOperacionRepository
                .findByProductoIdOrderByOrdenAsc(productoId)
                .stream()
                .map(po -> ProductoOperacionResponseDTO.builder()
                        .id(po.getId())
                        .productoId(productoId)
                        .productoSku(producto.getSku())
                        .productoNombre(producto.getNombre())
                        .operacionId(po.getOperacion().getId())
                        .operacionCodigo(po.getOperacion().getCodigo())
                        .operacionNombre(po.getOperacion().getNombre())
                        .tiempoOperacion(po.getOperacion().getTiempoOperacion())
                        .costoMinutoOperacion(po.getOperacion().getCostoMinuto())
                        .centroTrabajoNombre(po.getOperacion().getCentroTrabajo() != null
                                ? po.getOperacion().getCentroTrabajo().getNombre() : null)
                        .cantidad(po.getCantidad())
                        .tiempoTotal(po.getTiempoTotal())
                        .importeActividad(po.getImporteActividad())
                        .orden(po.getOrden())
                        .observaciones(po.getObservaciones())
                        .activo(po.getActivo())
                        .build())
                .collect(Collectors.toList());

        double costoInsumosBase = insumos.stream()
                .mapToDouble(item -> {
                    double costoUnitario = nz(item.getCostoUnitario());
                    double cantidad = nz(item.getCantidad());
                    return costoUnitario * cantidad;
                })
                .sum();

        double costoInsumosConDesperdicio = insumos.stream()
                .mapToDouble(item -> nz(item.getSubtotal()))
                .sum();

        double costoOperacionesSeguro = nz(productoOperacionRepository.sumarCostoTotalByProducto(productoId));
        double tiempoOperacionesMinutos = operaciones.stream()
                .mapToDouble(item -> nz(item.getTiempoTotal()))
                .sum();
        CifResumenDTO resumenCif = costoIndirectoService.obtenerResumen();
        double totalMensualCif = nz(resumenCif.getTotalMensual());
        double tasaCifMinuto = nz(resumenCif.getCostoMinuto());
        double costoCif = tiempoOperacionesMinutos * tasaCifMinuto;
        List<ProductoCostoIndirectoDTO> costosIndirectos = construirDetalleCif(
                tiempoOperacionesMinutos,
                totalMensualCif,
                nz(resumenCif.getMinutosProductivosMes()));

        double costoPrimo = costoInsumosConDesperdicio + costoOperacionesSeguro;
        double costoTotal = costoPrimo + costoCif;

        return ProductoEstructuraCostosDTO.builder()
                .productoId(producto.getId())
                .productoSku(producto.getSku())
                .productoNombre(producto.getNombre())
                .costoInsumosBase(costoInsumosBase)
                .costoInsumosConDesperdicio(costoInsumosConDesperdicio)
                .costoOperaciones(costoOperacionesSeguro)
                .costoPrimo(costoPrimo)
                .costoCif(costoCif)
                .costoTotal(costoTotal)
                .tiempoOperacionesMinutos(tiempoOperacionesMinutos)
                .tasaCifMinuto(tasaCifMinuto)
                .cifMensual(totalMensualCif)
                .minutosProductivosMes(nz(resumenCif.getMinutosProductivosMes()))
                .configuracionCifId(resumenCif.getConfiguracionId())
                .anioCif(null)
                .mesCif(null)
                .insumos(insumos)
                .operaciones(operaciones)
                .costosIndirectos(costosIndirectos)
                .build();
    }

    @Transactional(readOnly = true)
    public List<ProductoResponseDTO> listar() {
        return productoRepository.findAll()
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProductoResponseDTO> listarActivos() {
        return productoRepository.findByActivoTrue()
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProductoResponseDTO> buscar(String nombre) {
        return productoRepository.buscarPorNombre(nombre)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void eliminar(Long id) {
        ProductoModel producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));

        producto.setActivo(false);
        productoRepository.save(producto);
    }

    @Transactional
    public void activar(Long id) {
        ProductoModel producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));

        producto.setActivo(true);
        productoRepository.save(producto);
    }

    @Transactional(readOnly = true)
    public Double calcularCostoProducto(Long productoId) {
        List<ProductoInsumoModel> insumos = productoInsumoRepository.findByProductoId(productoId);
        validarCostosCotizacion(insumos);
        
        return insumos.stream()
                .mapToDouble(pi -> {
                    double costoUnitario = obtenerCostoCotizacionValido(pi.getInsumo());
                    return nz(pi.getCantidad()) * costoUnitario;
                })
                .sum();
    }

    @Transactional(readOnly = true)
    public Double calcularCostoProductoConDesperdicio(Long productoId) {
        List<ProductoInsumoModel> insumos = productoInsumoRepository.findByProductoId(productoId);
        validarCostosCotizacion(insumos);
        
        return insumos.stream()
                .mapToDouble(pi -> {
                    double costoUnitario = obtenerCostoCotizacionValido(pi.getInsumo());
                    double cantidadConDesperdicio = nz(pi.getCantidad()) * (1 + nz(pi.getDesperdicioPorcentaje()) / 100);
                    return cantidadConDesperdicio * costoUnitario;
                })
                .sum();
    }

    private ProductoResponseDTO mapToResponseDTO(ProductoModel producto) {
        List<ProductoInsumoResponseDTO> insumos = productoInsumoRepository.findByProductoId(producto.getId())
            .stream()
            .map(pi -> {
                double costoUnitario = obtenerCostoCotizacionOpcional(pi.getInsumo());
                double cantidadConDesperdicio = nz(pi.getCantidad()) * (1 + nz(pi.getDesperdicioPorcentaje()) / 100);

                return ProductoInsumoResponseDTO.builder()
                        .id(pi.getId())
                        .productoId(producto.getId())
                        .insumoId(pi.getInsumo().getId())
                        .insumoNombre(pi.getInsumo().getNombre())
                        .insumoUnidad(pi.getInsumo().getUnidadMedida().getSimbolo())
                        .cantidad(pi.getCantidad())
                        .desperdicioPorcentaje(pi.getDesperdicioPorcentaje())
                        .cantidadConDesperdicio(cantidadConDesperdicio)
                        .observaciones(pi.getObservaciones())
                        .costoUnitario(costoUnitario)
                        .subtotal(cantidadConDesperdicio * costoUnitario)
                        .fechaRegistro(pi.getFechaRegistro())
                        .fechaActualizacion(pi.getFechaActualizacion())
                        .build();
            })
            .collect(Collectors.toList());

        List<ProductoOperacionResponseDTO> operaciones = productoOperacionRepository
            .findByProductoIdOrderByOrdenAsc(producto.getId())
            .stream()
            .map(po -> ProductoOperacionResponseDTO.builder()
                    .id(po.getId())
                    .operacionId(po.getOperacion().getId())
                    .operacionCodigo(po.getOperacion().getCodigo())
                    .operacionNombre(po.getOperacion().getNombre())
                    .tiempoOperacion(po.getOperacion().getTiempoOperacion())
                    .costoMinutoOperacion(po.getOperacion().getCostoMinuto())
                    .centroTrabajoNombre(po.getOperacion().getCentroTrabajo() != null ? 
                        po.getOperacion().getCentroTrabajo().getNombre() : null)
                    .cantidad(po.getCantidad())
                    .tiempoTotal(po.getTiempoTotal())
                    .importeActividad(po.getImporteActividad())
                    .orden(po.getOrden())
                    .observaciones(po.getObservaciones())
                    .build())
            .collect(Collectors.toList());

        List<ImagenResponseDTO> imagenes = imagenService.obtenerPorProducto(producto.getId());
        ModeloModel modelo = producto.getModelo();
        FamiliaModel familia = modelo != null ? modelo.getFamilia() : null;
        LineaModel linea = familia != null ? familia.getLinea() : null;

        ProductoResponseDTO response = new ProductoResponseDTO();
        response.setId(producto.getId());
        response.setSku(producto.getSku());
        response.setNombre(producto.getNombre());
        response.setDescripcion(producto.getDescripcion());
        response.setDescripcionCorta(producto.getDescripcionCorta());
        response.setPesoVolumetrico(producto.getPesoVolumetrico());
        response.setAncho(producto.getAncho());
        response.setAlto(producto.getAlto());
        response.setFondo(producto.getFondo());
        response.setDimensiones(producto.getDimensiones());
        response.setPesoKg(producto.getPesoKg());
        response.setModeloId(modelo != null ? modelo.getId() : null);
        response.setModeloNombre(modelo != null ? modelo.getNombre() : null);
        response.setModeloUrlImagen(modelo != null ? modelo.getUrlImagen() : null);
        response.setFamiliaId(familia != null ? familia.getId() : null);
        response.setFamiliaNombre(familia != null ? familia.getNombre() : null);
        response.setSubfamiliaId(modelo != null && modelo.getSubfamilia() != null ? modelo.getSubfamilia().getId() : null);
        response.setSubfamiliaNombre(modelo != null && modelo.getSubfamilia() != null ? modelo.getSubfamilia().getNombre() : null);
        response.setLineaId(linea != null ? linea.getId() : null);
        response.setLineaNombre(linea != null ? linea.getNombre() : null);
        response.setNivelId(producto.getNivel() != null ? producto.getNivel().getId() : null);
        response.setNivelNombre(producto.getNivel() != null ? producto.getNivel().getNombre() : null);
        response.setColorId(producto.getColor() != null ? producto.getColor().getId() : null);
        response.setColorNombre(producto.getColor() != null ? producto.getColor().getNombre() : null);
        response.setColorCodigo(producto.getColor() != null ? producto.getColor().getCodigo() : null);
        response.setColorHex(producto.getColor() != null ? producto.getColor().getHex() : null);
        response.setMaterialId(producto.getMaterial() != null ? producto.getMaterial().getId() : null);
        response.setMaterialNombre(producto.getMaterial() != null ? producto.getMaterial().getNombre() : null);
        response.setActivo(producto.getActivo());
        response.setCreatedAt(producto.getCreatedAt());
        response.setUpdatedAt(producto.getUpdatedAt());
        response.setImagenPrincipal(imagenService.obtenerPrincipalPorProducto(producto.getId()));
        response.setImagenes(imagenes);
        response.setInsumos(insumos);
        response.setOperaciones(operaciones);
        return response;
    }

    @Transactional(readOnly = true)
    public List<ProductoResponseDTO> obtenerTodosCompletos() {
        return productoRepository.findAll()
                .stream()
                .filter(this::esProductoVisible)
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PageResponseDTO<ProductoResponseDTO> obtenerTodosCompletosPaginado(
            Boolean activo,
            String busqueda,
            Long modeloId,
            Long nivelId,
            Long colorId,
            Pageable pageable) {
        Page<ProductoResponseDTO> page = productoRepository
                .buscarPaginado(activo, normalizarFiltro(busqueda), modeloId, nivelId, colorId, pageable)
                .map(this::mapToResponseDTO);

        return new PageResponseDTO<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages());
    }

    @Transactional(readOnly = true)
    public ProductoResponseDTO obtenerProductoCompleto(Long id) {
        ProductoModel producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));
        return mapToResponseDTO(producto);
    }

    @Transactional(readOnly = true)
    public ProductoResponseDTO obtenerProductoCompletoPorSku(String sku) {
        ProductoModel producto = productoRepository.findBySku(sku)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con SKU: " + sku));
        return mapToResponseDTO(producto);
    }

    @Transactional(readOnly = true)
    public List<ProductoResponseDTO> obtenerCompletosPorModelo(Long modeloId) {
        return productoRepository.findByModeloId(modeloId)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProductoFichaDTO obtenerFichaPorModelo(Long modeloId) {
        ModeloModel modelo = modeloRepository.findById(modeloId)
                .orElseThrow(() -> new ResourceNotFoundException("Modelo no encontrado: " + modeloId));

        List<ProductoModel> productos = productoRepository.findByModeloId(modeloId);

        return construirFicha(modelo, productos);
    }

    /**
     * Version PUBLICA de la ficha: solo modelo activo y variantes activas.
     * Si el modelo no existe o esta inactivo, se responde 404 (no se expone al catalogo publico).
     */
    @Transactional(readOnly = true)
    public ProductoFichaDTO obtenerFichaPublicaPorModelo(Long modeloId) {
        ModeloModel modelo = modeloRepository.findById(modeloId)
                .filter(m -> Boolean.TRUE.equals(m.getActivo()))
                .orElseThrow(() -> new ResourceNotFoundException("Modelo no encontrado: " + modeloId));

        List<ProductoModel> productos = productoRepository.findByModeloId(modeloId).stream()
                .filter(producto -> Boolean.TRUE.equals(producto.getActivo()))
                .collect(Collectors.toList());

        return construirFicha(modelo, productos);
    }

    /**
     * Listado ligero para alimentar el grid del catalogo PUBLICO (solo modelos activos).
     * Filtros opcionales y combinables: familia, subfamilia, color y tamano (nivel).
     * Un modelo entra si tiene al menos una variante activa que cumpla a la vez color y tamano.
     */
    @Transactional(readOnly = true)
    public List<ModeloCatalogoPublicoDTO> listarCatalogoPublico(Long familiaId, Long subfamiliaId,
                                                                Long colorId, Long tamanoId) {
        Map<Long, List<ProductoModel>> variantesPorModelo = variantesActivasPorModelo();

        return modelosActivosFiltrados(familiaId, subfamiliaId)
                .filter(modelo -> cumpleFiltrosDeVariante(
                        variantesPorModelo.get(modelo.getId()), colorId, tamanoId))
                .map(modelo -> ModeloCatalogoPublicoDTO.builder()
                        .modeloId(modelo.getId())
                        .codigo(modelo.getCodigo())
                        .nombre(modelo.getNombre())
                        .descripcion(modelo.getDescripcion())
                        .urlImagenModelo(modelo.getUrlImagen())
                        .familiaId(modelo.getFamilia() != null ? modelo.getFamilia().getId() : null)
                        .familiaNombre(modelo.getFamilia() != null ? modelo.getFamilia().getNombre() : null)
                        .subfamiliaId(modelo.getSubfamilia() != null ? modelo.getSubfamilia().getId() : null)
                        .subfamiliaNombre(modelo.getSubfamilia() != null ? modelo.getSubfamilia().getNombre() : null)
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Facetas del catalogo PUBLICO: opciones de subfamilia, color y tamano con conteo de muebles.
     * El conteo es de MODELOS activos con al menos una variante activa que tenga la opcion.
     * La faceta de subfamilias respeta solo familiaId (para poder listar las subfamilias hermanas);
     * colores y tamanos respetan familiaId y subfamiliaId.
     */
    @Transactional(readOnly = true)
    public CatalogoFacetasDTO obtenerFacetasPublicas(Long familiaId, Long subfamiliaId) {
        Map<Long, List<ProductoModel>> variantesPorModelo = variantesActivasPorModelo();

        Map<Long, CatalogoFacetasDTO.SubfamiliaFacetaDTO> subfamilias = new LinkedHashMap<>();
        Map<Long, Long> conteoSubfamilias = new LinkedHashMap<>();
        modelosActivosFiltrados(familiaId, null).forEach(modelo -> {
            if (modelo.getSubfamilia() == null || modelo.getSubfamilia().getId() == null) {
                return;
            }
            Long id = modelo.getSubfamilia().getId();
            subfamilias.computeIfAbsent(id, k -> CatalogoFacetasDTO.SubfamiliaFacetaDTO.builder()
                    .id(id)
                    .nombre(modelo.getSubfamilia().getNombre())
                    .build());
            conteoSubfamilias.merge(id, 1L, Long::sum);
        });
        subfamilias.forEach((id, faceta) -> faceta.setConteo(conteoSubfamilias.get(id)));

        Map<Long, CatalogoFacetasDTO.ColorFacetaDTO> colores = new LinkedHashMap<>();
        Map<Long, Long> conteoColores = new LinkedHashMap<>();
        Map<Long, CatalogoFacetasDTO.TamanoFacetaDTO> tamanos = new LinkedHashMap<>();
        Map<Long, Long> conteoTamanos = new LinkedHashMap<>();

        modelosActivosFiltrados(familiaId, subfamiliaId).forEach(modelo -> {
            List<ProductoModel> variantes = variantesPorModelo.getOrDefault(modelo.getId(), List.of());

            Map<Long, ColorModel> coloresDelModelo = new LinkedHashMap<>();
            Map<Long, NivelModel> tamanosDelModelo = new LinkedHashMap<>();
            for (ProductoModel variante : variantes) {
                if (variante.getColor() != null && variante.getColor().getId() != null) {
                    coloresDelModelo.putIfAbsent(variante.getColor().getId(), variante.getColor());
                }
                if (variante.getNivel() != null && variante.getNivel().getId() != null) {
                    tamanosDelModelo.putIfAbsent(variante.getNivel().getId(), variante.getNivel());
                }
            }

            coloresDelModelo.forEach((id, color) -> {
                colores.computeIfAbsent(id, k -> CatalogoFacetasDTO.ColorFacetaDTO.builder()
                        .id(id)
                        .nombre(color.getNombre())
                        .hex(color.getHex())
                        .build());
                conteoColores.merge(id, 1L, Long::sum);
            });
            tamanosDelModelo.forEach((id, nivel) -> {
                tamanos.computeIfAbsent(id, k -> CatalogoFacetasDTO.TamanoFacetaDTO.builder()
                        .id(id)
                        .nombre(nivel.getNombre())
                        .build());
                conteoTamanos.merge(id, 1L, Long::sum);
            });
        });
        colores.forEach((id, faceta) -> faceta.setConteo(conteoColores.get(id)));
        tamanos.forEach((id, faceta) -> faceta.setConteo(conteoTamanos.get(id)));

        return CatalogoFacetasDTO.builder()
                .subfamilias(new ArrayList<>(subfamilias.values()))
                .colores(new ArrayList<>(colores.values()))
                .tamanos(new ArrayList<>(tamanos.values()))
                .build();
    }

    // Variantes activas agrupadas por id de modelo (base comun del listado publico y las facetas).
    private Map<Long, List<ProductoModel>> variantesActivasPorModelo() {
        return productoRepository.findByActivoTrue().stream()
                .filter(producto -> producto.getModelo() != null && producto.getModelo().getId() != null)
                .collect(Collectors.groupingBy(producto -> producto.getModelo().getId()));
    }

    private Stream<ModeloModel> modelosActivosFiltrados(Long familiaId, Long subfamiliaId) {
        return modeloRepository.findByActivo(true).stream()
                .filter(modelo -> familiaId == null
                        || (modelo.getFamilia() != null && familiaId.equals(modelo.getFamilia().getId())))
                .filter(modelo -> subfamiliaId == null
                        || (modelo.getSubfamilia() != null && subfamiliaId.equals(modelo.getSubfamilia().getId())));
    }

    // Sin filtros de color/tamano todo modelo cumple; con filtros, debe existir una variante activa
    // que satisfaga AMBOS a la vez.
    private boolean cumpleFiltrosDeVariante(List<ProductoModel> variantes, Long colorId, Long tamanoId) {
        if (colorId == null && tamanoId == null) {
            return true;
        }
        if (variantes == null || variantes.isEmpty()) {
            return false;
        }
        return variantes.stream().anyMatch(variante ->
                (colorId == null || (variante.getColor() != null
                        && colorId.equals(variante.getColor().getId())))
                && (tamanoId == null || (variante.getNivel() != null
                        && tamanoId.equals(variante.getNivel().getId()))));
    }

    // Construye la ficha agregada (imagenes, colores, tamanos, materiales y variantes) a partir
    // de un modelo y su lista de productos. Compartido por la version interna y la publica.
    private ProductoFichaDTO construirFicha(ModeloModel modelo, List<ProductoModel> productos) {
        List<ImagenResponseDTO> imagenes = new ArrayList<>();
        Map<Long, ProductoFichaDTO.ColorOpcionDTO> colores = new LinkedHashMap<>();
        Map<Long, ProductoFichaDTO.TamanoOpcionDTO> tamanos = new LinkedHashMap<>();
        Map<Long, ProductoFichaDTO.MaterialOpcionDTO> materiales = new LinkedHashMap<>();
        List<ProductoFichaDTO.VarianteFichaDTO> variantes = new ArrayList<>();

        for (ProductoModel producto : productos) {
            imagenes.addAll(imagenService.obtenerPorProducto(producto.getId()));

            ColorModel color = producto.getColor();
            if (color != null && color.getId() != null) {
                colores.computeIfAbsent(color.getId(), id -> ProductoFichaDTO.ColorOpcionDTO.builder()
                        .id(color.getId())
                        .codigo(color.getCodigo())
                        .nombre(color.getNombre())
                        .hex(color.getHex())
                        .build());
            }

            NivelModel nivel = producto.getNivel();
            if (nivel != null && nivel.getId() != null) {
                tamanos.computeIfAbsent(nivel.getId(), id -> ProductoFichaDTO.TamanoOpcionDTO.builder()
                        .id(nivel.getId())
                        .codigo(nivel.getCodigo())
                        .nombre(nivel.getNombre())
                        .categoriaNombre(nivel.getCategoria() != null ? nivel.getCategoria().getNombre() : null)
                        .build());
            }

            MaterialModel material = producto.getMaterial();
            if (material != null && material.getId() != null) {
                materiales.computeIfAbsent(material.getId(), id -> ProductoFichaDTO.MaterialOpcionDTO.builder()
                        .id(material.getId())
                        .codigo(material.getCodigo())
                        .nombre(material.getNombre())
                        .build());
            }

            ImagenResponseDTO imagenPrincipal = imagenService.obtenerPrincipalPorProducto(producto.getId());

            variantes.add(ProductoFichaDTO.VarianteFichaDTO.builder()
                    .productoId(producto.getId())
                    .sku(producto.getSku())
                    .nivelId(nivel != null ? nivel.getId() : null)
                    .nivelNombre(nivel != null ? nivel.getNombre() : null)
                    .colorId(color != null ? color.getId() : null)
                    .colorNombre(color != null ? color.getNombre() : null)
                    .colorHex(color != null ? color.getHex() : null)
                    .materialId(material != null ? material.getId() : null)
                    .materialNombre(material != null ? material.getNombre() : null)
                    .ancho(producto.getAncho())
                    .alto(producto.getAlto())
                    .fondo(producto.getFondo())
                    .dimensiones(producto.getDimensiones())
                    .pesoKg(producto.getPesoKg())
                    .pesoVolumetrico(producto.getPesoVolumetrico())
                    .caracteristicas(producto.getCaracteristicas())
                    .imagenPrincipalUrl(imagenPrincipal != null ? imagenPrincipal.getUrl() : null)
                    .activo(producto.getActivo())
                    .build());
        }

        FamiliaModel familia = modelo.getFamilia();

        return ProductoFichaDTO.builder()
                .modeloId(modelo.getId())
                .codigo(modelo.getCodigo())
                .nombre(modelo.getNombre())
                .descripcion(modelo.getDescripcion())
                .urlImagenModelo(modelo.getUrlImagen())
                .familiaId(familia != null ? familia.getId() : null)
                .familiaNombre(familia != null ? familia.getNombre() : null)
                .subfamiliaId(modelo.getSubfamilia() != null ? modelo.getSubfamilia().getId() : null)
                .subfamiliaNombre(modelo.getSubfamilia() != null ? modelo.getSubfamilia().getNombre() : null)
                .imagenes(imagenes)
                .colores(new ArrayList<>(colores.values()))
                .tamanos(new ArrayList<>(tamanos.values()))
                .materiales(new ArrayList<>(materiales.values()))
                .variantes(variantes)
                .build();
    }

    @Transactional(readOnly = true)
    public List<ProductoResponseDTO> buscarCompletasConFiltros(String sku, String nombre, Long modeloId,
                                                                       Long nivelId, Long colorId) {
        return productoRepository.buscarConFiltros(sku, nombre, modeloId, nivelId, colorId, null)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void eliminarProducto(Long id) {
        ProductoModel producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));

        imagenService.eliminarArchivosFisicosPorProducto(id);
        productoRepository.delete(producto);
    }

    private org.springframework.data.domain.Sort construirSortProductos(String sortBy, String direction) {
        org.springframework.data.domain.Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction)
                ? org.springframework.data.domain.Sort.Direction.DESC
                : org.springframework.data.domain.Sort.Direction.ASC;

        if (sortBy == null || sortBy.isBlank()) {
            return TypeSafeSorts.ascWithId(ProductoModel.class, ProductoModel::getSku, ProductoModel::getId);
        }

        String campo = sortBy.trim().toLowerCase(Locale.ROOT);
        return switch (campo) {
            case "id" -> sortDirection == org.springframework.data.domain.Sort.Direction.DESC
                    ? TypeSafeSorts.descById(ProductoModel.class, ProductoModel::getId)
                    : TypeSafeSorts.ascById(ProductoModel.class, ProductoModel::getId);
            case "sku" -> sortDirection == org.springframework.data.domain.Sort.Direction.DESC
                    ? TypeSafeSorts.descWithId(ProductoModel.class, ProductoModel::getSku, ProductoModel::getId)
                    : TypeSafeSorts.ascWithId(ProductoModel.class, ProductoModel::getSku, ProductoModel::getId);
            case "nombre" -> sortDirection == org.springframework.data.domain.Sort.Direction.DESC
                    ? TypeSafeSorts.descWithId(ProductoModel.class, ProductoModel::getNombre, ProductoModel::getId)
                    : TypeSafeSorts.ascWithId(ProductoModel.class, ProductoModel::getNombre, ProductoModel::getId);
            case "descripcion" -> sortDirection == org.springframework.data.domain.Sort.Direction.DESC
                    ? TypeSafeSorts.descWithId(ProductoModel.class, ProductoModel::getDescripcion, ProductoModel::getId)
                    : TypeSafeSorts.ascWithId(ProductoModel.class, ProductoModel::getDescripcion, ProductoModel::getId);
            case "activo" -> sortDirection == org.springframework.data.domain.Sort.Direction.DESC
                    ? TypeSafeSorts.descWithId(ProductoModel.class, ProductoModel::getActivo, ProductoModel::getId)
                    : TypeSafeSorts.ascWithId(ProductoModel.class, ProductoModel::getActivo, ProductoModel::getId);
            case "createdat", "created_at" -> sortDirection == org.springframework.data.domain.Sort.Direction.DESC
                    ? TypeSafeSorts.descWithId(ProductoModel.class, ProductoModel::getCreatedAt, ProductoModel::getId)
                    : TypeSafeSorts.ascWithId(ProductoModel.class, ProductoModel::getCreatedAt, ProductoModel::getId);
            case "updatedat", "updated_at" -> sortDirection == org.springframework.data.domain.Sort.Direction.DESC
                    ? TypeSafeSorts.descWithId(ProductoModel.class, ProductoModel::getUpdatedAt, ProductoModel::getId)
                    : TypeSafeSorts.ascWithId(ProductoModel.class, ProductoModel::getUpdatedAt, ProductoModel::getId);
            default -> TypeSafeSorts.ascWithId(ProductoModel.class, ProductoModel::getSku, ProductoModel::getId);
        };
    }

    private boolean coincideBusqueda(ProductoModel producto, String busqueda) {
        if (busqueda == null || busqueda.isBlank()) {
            return true;
        }

        String termino = busqueda.trim().toLowerCase(Locale.ROOT);
        return Stream.of(
                        producto.getId() != null ? String.valueOf(producto.getId()) : null,
                        producto.getSku(),
                        producto.getNombre(),
                        producto.getDescripcion(),
                        producto.getDescripcionCorta(),
                        producto.getPesoVolumetrico() != null ? String.valueOf(producto.getPesoVolumetrico()) : null,
                        producto.getAncho() != null ? String.valueOf(producto.getAncho()) : null,
                        producto.getAlto() != null ? String.valueOf(producto.getAlto()) : null,
                        producto.getFondo() != null ? String.valueOf(producto.getFondo()) : null,
                        producto.getCaracteristicas(),
                        producto.getDimensiones(),
                        producto.getModelo() != null ? producto.getModelo().getCodigo() : null,
                        producto.getModelo() != null ? producto.getModelo().getNombre() : null,
                        producto.getModelo() != null && producto.getModelo().getFamilia() != null
                                ? producto.getModelo().getFamilia().getNombre() : null,
                        producto.getModelo() != null && producto.getModelo().getFamilia() != null
                                && producto.getModelo().getFamilia().getLinea() != null
                                ? producto.getModelo().getFamilia().getLinea().getNombre() : null,
                        producto.getNivel() != null ? producto.getNivel().getNombre() : null,
                        producto.getMaterial() != null ? producto.getMaterial().getNombre() : null,
                        producto.getColor() != null ? producto.getColor().getNombre() : null,
                        producto.getActivo() != null ? (producto.getActivo() ? "activo" : "inactivo") : null,
                        producto.getCreatedAt() != null ? producto.getCreatedAt().toString() : null,
                        producto.getUpdatedAt() != null ? producto.getUpdatedAt().toString() : null)
                .filter(valor -> valor != null && !valor.isBlank())
                .map(valor -> valor.toLowerCase(Locale.ROOT))
                .anyMatch(valor -> valor.contains(termino));
    }

    private String nvl(String value) {
        return value == null ? "" : value;
    }

    private void validarCostosCotizacion(List<ProductoInsumoModel> insumos) {
        List<String> faltantes = insumos.stream()
                .map(ProductoInsumoModel::getInsumo)
                .filter(insumo -> obtenerCostoCotizacionOpcional(insumo) <= 0)
                .map(InsumoModel::getNombre)
                .filter(nombre -> nombre != null && !nombre.isBlank())
                .distinct()
                .collect(Collectors.toList());

        if (!faltantes.isEmpty()) {
            throw new ValidationException("Faltan costos de cotizacion para: " + String.join(", ", faltantes));
        }
    }

    private double obtenerCostoCotizacionValido(InsumoModel insumo) {
        double costoCotizacion = obtenerCostoCotizacionOpcional(insumo);
        if (costoCotizacion <= 0) {
            String nombre = insumo != null && insumo.getNombre() != null ? insumo.getNombre() : "insumo sin nombre";
            throw new ValidationException("Faltan costos de cotizacion para: " + nombre);
        }
        return costoCotizacion;
    }

    private double obtenerCostoCotizacionOpcional(InsumoModel insumo) {
        return insumo != null && insumo.getCostoCotizacion() != null ? insumo.getCostoCotizacion() : 0.0;
    }

    private double nz(Double value) {
        return value == null ? 0.0 : value;
    }

    private List<ProductoCostoIndirectoDTO> construirDetalleCif(
            double tiempoOperacionesMinutos,
            double totalMensualCif,
            double minutosProductivosMes) {
        return costoIndirectoService.listarActivos()
                .stream()
                .map(costo -> {
                    double montoMensual = nz(costo.getMontoMensualEquivalente());
                    double costoMinuto = minutosProductivosMes > 0 ? montoMensual / minutosProductivosMes : 0.0;
                    double montoAsignado = costoMinuto * tiempoOperacionesMinutos;
                    double participacion = totalMensualCif > 0 ? (montoMensual / totalMensualCif) * 100.0 : 0.0;

                    return ProductoCostoIndirectoDTO.builder()
                            .id(costo.getId())
                            .costoIndirectoCodigo(costo.getCodigo())
                            .costoIndirectoNombre(costo.getNombre())
                            .tipo(costo.getTipo() != null ? costo.getTipo().name() : null)
                            .periodicidad(costo.getPeriodicidad() != null ? costo.getPeriodicidad().name() : null)
                            .monto(nz(costo.getMonto()))
                            .montoMensual(montoMensual)
                            .costoMinuto(costoMinuto)
                            .porcentajeParticipacion(participacion)
                            .baseCalculo(String.format(Locale.ROOT, "%.2f min x %.4f/min", tiempoOperacionesMinutos, costoMinuto))
                            .montoAsignado(montoAsignado)
                            .build();
                })
                .collect(Collectors.toList());
    }

    private String generarSkuProducto(ModeloModel modelo, NivelModel nivel, MaterialModel material, ColorModel color) {
        if (modelo.getFamilia() == null || modelo.getFamilia().getLinea() == null) {
            throw new ValidationException("El modelo debe tener familia y linea para generar el sku");
        }

        String lineaCodigo = modelo.getFamilia().getLinea().getCodigo();
        String familiaCodigo = modelo.getFamilia().getCodigo();
        String subfamiliaCodigo = modelo.getSubfamilia() != null ? modelo.getSubfamilia().getCodigo() : "";
        String modeloCodigo = modelo.getCodigo();
        String nivelCodigo = nivel.getCodigo();
        String materialCodigo = material.getCodigo();
        String colorCodigo = color.getCodigo();

        if (lineaCodigo == null || familiaCodigo == null || modeloCodigo == null || nivelCodigo == null
                || materialCodigo == null || colorCodigo == null) {
            throw new ValidationException("Faltan codigos requeridos para generar el sku del producto");
        }

        return (lineaCodigo + familiaCodigo + subfamiliaCodigo + modeloCodigo + "-" + nivelCodigo + "-" + materialCodigo + "-" + colorCodigo).toUpperCase();
    }

    private Long id(ModeloModel item) {
        return item != null ? item.getId() : null;
    }

    private Long id(NivelModel item) {
        return item != null ? item.getId() : null;
    }

    private Long id(MaterialModel item) {
        return item != null ? item.getId() : null;
    }

    private Long id(ColorModel item) {
        return item != null ? item.getId() : null;
    }

    private void validarNivelDelModelo(ModeloModel modelo, NivelModel nivel) {
        if (modelo == null || nivel == null || nivel.getModelo() == null
                || !Objects.equals(modelo.getId(), nivel.getModelo().getId())) {
            throw new ValidationException("La categoria seleccionada no pertenece al modelo");
        }
    }

    private boolean esProductoVisible(ProductoModel producto) {
        return producto.getModelo() != null || producto.getNivel() != null || producto.getMaterial() != null || producto.getColor() != null;
    }

    private String normalizarFiltro(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim().replaceAll("\\s+", " ");
    }
}
