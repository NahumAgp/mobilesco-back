package com.mobilesco.mobilesco_back.services;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mobilesco.mobilesco_back.dto.Producto.ProductoCreateDTO;
import com.mobilesco.mobilesco_back.dto.Producto.ProductoInsumoResponseDTO;
import com.mobilesco.mobilesco_back.dto.Producto.ProductoEstructuraCostosDTO;
import com.mobilesco.mobilesco_back.dto.Producto.ProductoResponseDTO;
import com.mobilesco.mobilesco_back.dto.Producto.ProductoUpdateDTO;
import com.mobilesco.mobilesco_back.dto.imagen.ImagenResponseDTO;
import com.mobilesco.mobilesco_back.dto.ProductoOperacion.ProductoOperacionResponseDTO;
import com.mobilesco.mobilesco_back.exceptions.ResourceNotFoundException;
import com.mobilesco.mobilesco_back.exceptions.ValidationException;
import com.mobilesco.mobilesco_back.models.ColorModel;
import com.mobilesco.mobilesco_back.models.FamiliaModel;
import com.mobilesco.mobilesco_back.models.LineaModel;
import com.mobilesco.mobilesco_back.models.MaterialModel;
import com.mobilesco.mobilesco_back.models.ModeloModel;
import com.mobilesco.mobilesco_back.models.NivelModel;
import com.mobilesco.mobilesco_back.models.ProductoInsumoModel;
import com.mobilesco.mobilesco_back.models.ProductoModel;
import com.mobilesco.mobilesco_back.repositories.ColorRepository;
import com.mobilesco.mobilesco_back.repositories.MaterialRepository;
import com.mobilesco.mobilesco_back.repositories.NivelRepository;
import com.mobilesco.mobilesco_back.repositories.ModeloRepository;
import com.mobilesco.mobilesco_back.repositories.ProductoInsumoRepository;
import com.mobilesco.mobilesco_back.repositories.ProductoOperacionRepository;
import com.mobilesco.mobilesco_back.repositories.ProductoRepository;

@Service
public class ProductoService {

    private final ProductoRepository productoRepository;
    
    private final ModeloRepository modeloRepository;
    private final NivelRepository nivelRepository;
    private final ColorRepository colorRepository;
    private final MaterialRepository materialRepository;
    private final ProductoInsumoRepository productoInsumoRepository;
    private final ProductoOperacionRepository productoOperacionRepository;
    private final KardexService kardexService;
    private final ImagenService imagenService;

    public ProductoService(
            ProductoRepository productoRepository,
            ModeloRepository modeloRepository,
            NivelRepository nivelRepository,
            ColorRepository colorRepository,
            MaterialRepository materialRepository,
            ProductoInsumoRepository productoInsumoRepository,
            ProductoOperacionRepository productoOperacionRepository,
            KardexService kardexService,
            ImagenService imagenService) {
        this.productoRepository = productoRepository;
        this.modeloRepository = modeloRepository;
        this.nivelRepository = nivelRepository;
        this.colorRepository = colorRepository;
        this.materialRepository = materialRepository;
        this.productoInsumoRepository = productoInsumoRepository;
        this.productoOperacionRepository = productoOperacionRepository;
        this.kardexService = kardexService;
        this.imagenService = imagenService;
    }

    @Transactional
    public ProductoResponseDTO crear(ProductoCreateDTO dto) {
        ModeloModel modelo = modeloRepository.findById(dto.getModeloId())
                .orElseThrow(() -> new ResourceNotFoundException("Modelo no encontrado"));

        NivelModel nivel = nivelRepository.findById(dto.getNivelId())
                .orElseThrow(() -> new ResourceNotFoundException("Nivel no encontrado"));

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
                .modelo(modelo)
                .nivel(nivel)
                .material(material)
                .color(color)
                .activo(dto.getActivo() == null || Boolean.TRUE.equals(dto.getActivo()))
                .build();

        ProductoModel saved = productoRepository.save(producto);
        return mapToResponseDTO(saved);
    }

    @Transactional(readOnly = true)
    public byte[] generarReporteExcel(Boolean activo, String busqueda, String sortBy, String direction) {
        List<ProductoModel> productos = productoRepository.findAll(construirSortProductos(sortBy, direction));

        List<ProductoModel> filtrados = productos.stream()
                .filter(producto -> activo == null || Objects.equals(producto.getActivo(), activo))
                .filter(producto -> coincideBusqueda(producto, busqueda))
                .collect(Collectors.toList());

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Productos");

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            String[] headers = {
                    "ID", "SKU", "Nombre", "Descripcion", "Modelo", "Familia", "Linea", "Nivel", "Material", "Color", "Estado", "Creado", "Actualizado"
            };

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIndex = 1;
            for (ProductoModel producto : filtrados) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(producto.getId() != null ? producto.getId().doubleValue() : 0.0);
                row.createCell(1).setCellValue(nvl(producto.getSku()));
                row.createCell(2).setCellValue(nvl(producto.getNombre()));
                row.createCell(3).setCellValue(nvl(producto.getDescripcion()));
                row.createCell(4).setCellValue(producto.getModelo() != null ? nvl(producto.getModelo().getNombre()) : "");
                row.createCell(5).setCellValue(producto.getModelo() != null && producto.getModelo().getFamilia() != null
                        ? nvl(producto.getModelo().getFamilia().getNombre()) : "");
                row.createCell(6).setCellValue(producto.getModelo() != null
                        && producto.getModelo().getFamilia() != null
                        && producto.getModelo().getFamilia().getLinea() != null
                        ? nvl(producto.getModelo().getFamilia().getLinea().getNombre()) : "");
                row.createCell(7).setCellValue(producto.getNivel() != null ? nvl(producto.getNivel().getNombre()) : "");
                row.createCell(8).setCellValue(producto.getMaterial() != null ? nvl(producto.getMaterial().getNombre()) : "");
                row.createCell(9).setCellValue(producto.getColor() != null ? nvl(producto.getColor().getNombre()) : "");
                row.createCell(10).setCellValue(Boolean.TRUE.equals(producto.getActivo()) ? "Activo" : "Inactivo");
                row.createCell(11).setCellValue(producto.getCreatedAt() != null ? producto.getCreatedAt().toString() : "");
                row.createCell(12).setCellValue(producto.getUpdatedAt() != null ? producto.getUpdatedAt().toString() : "");
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("No se pudo generar el reporte de productos", e);
        }
    }

    @Transactional
    public ProductoResponseDTO actualizar(Long id, ProductoUpdateDTO dto) {
        ProductoModel producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));

        String skuGenerado = dto.getSku();
        if (!producto.getSku().equalsIgnoreCase(skuGenerado) &&
                productoRepository.existsBySkuIgnoreCase(skuGenerado)) {
            throw new ValidationException("Ya existe un producto con SKU: " + skuGenerado);
        }

        if (dto.getModeloId() != null) {
            ModeloModel modelo = modeloRepository.findById(dto.getModeloId())
                    .orElseThrow(() -> new ResourceNotFoundException("Modelo no encontrado"));
            producto.setModelo(modelo);
        } else {
            producto.setModelo(null);
        }

        if (dto.getNivelId() != null) {
            NivelModel nivel = nivelRepository.findById(dto.getNivelId())
                    .orElseThrow(() -> new ResourceNotFoundException("Nivel no encontrado"));
            producto.setNivel(nivel);
        } else {
            producto.setNivel(null);
        }

        if (dto.getColorId() != null) {
            ColorModel color = colorRepository.findById(dto.getColorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Color no encontrado"));
            producto.setColor(color);
        } else {
            producto.setColor(null);
        }

        if (dto.getMaterialId() != null) {
            MaterialModel material = materialRepository.findById(dto.getMaterialId())
                    .orElseThrow(() -> new ResourceNotFoundException("Material no encontrado"));
            producto.setMaterial(material);
        } else {
            producto.setMaterial(null);
        }

        producto.setSku(skuGenerado);
        producto.setNombre(dto.getNombre());
        producto.setDescripcion(dto.getDescripcion());
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

        List<ProductoInsumoResponseDTO> insumos = productoInsumoRepository.findByProductoId(productoId)
                .stream()
                .map(pi -> {
                    double costoUnitarioSeguro = nz(kardexService.calcularCostoPromedio(pi.getInsumo().getId()));
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

        double costoPrimo = costoInsumosConDesperdicio + costoOperacionesSeguro;
        double costoCif = 0.0;
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
                .anioCif(null)
                .mesCif(null)
                .insumos(insumos)
                .operaciones(operaciones)
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
        
        return insumos.stream()
                .mapToDouble(pi -> {
                    double costoUnitario = nz(kardexService.calcularCostoPromedio(pi.getInsumo().getId()));
                    return nz(pi.getCantidad()) * costoUnitario;
                })
                .sum();
    }

    @Transactional(readOnly = true)
    public Double calcularCostoProductoConDesperdicio(Long productoId) {
        List<ProductoInsumoModel> insumos = productoInsumoRepository.findByProductoId(productoId);
        
        return insumos.stream()
                .mapToDouble(pi -> {
                    double costoUnitario = nz(kardexService.calcularCostoPromedio(pi.getInsumo().getId()));
                    double cantidadConDesperdicio = nz(pi.getCantidad()) * (1 + nz(pi.getDesperdicioPorcentaje()) / 100);
                    return cantidadConDesperdicio * costoUnitario;
                })
                .sum();
    }

    private ProductoResponseDTO mapToResponseDTO(ProductoModel producto) {
        List<ProductoInsumoResponseDTO> insumos = productoInsumoRepository.findByProductoId(producto.getId())
            .stream()
            .map(pi -> {
                double costoUnitario = nz(kardexService.calcularCostoPromedio(pi.getInsumo().getId()));
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
        response.setModeloId(modelo != null ? modelo.getId() : null);
        response.setModeloNombre(modelo != null ? modelo.getNombre() : null);
        response.setModeloUrlImagen(modelo != null ? modelo.getUrlImagen() : null);
        response.setFamiliaId(familia != null ? familia.getId() : null);
        response.setFamiliaNombre(familia != null ? familia.getNombre() : null);
        response.setLineaId(linea != null ? linea.getId() : null);
        response.setLineaNombre(linea != null ? linea.getNombre() : null);
        response.setNivelId(producto.getNivel() != null ? producto.getNivel().getId() : null);
        response.setNivelNombre(producto.getNivel() != null ? producto.getNivel().getNombre() : null);
        response.setColorId(producto.getColor() != null ? producto.getColor().getId() : null);
        response.setColorNombre(producto.getColor() != null ? producto.getColor().getNombre() : null);
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
            return org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.ASC, "sku")
                    .and(org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.ASC, "id"));
        }

        String campo = sortBy.trim().toLowerCase(Locale.ROOT);
        return switch (campo) {
            case "id" -> org.springframework.data.domain.Sort.by(sortDirection, "id");
            case "sku" -> org.springframework.data.domain.Sort.by(sortDirection, "sku")
                    .and(org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.ASC, "id"));
            case "nombre" -> org.springframework.data.domain.Sort.by(sortDirection, "nombre")
                    .and(org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.ASC, "id"));
            case "descripcion" -> org.springframework.data.domain.Sort.by(sortDirection, "descripcion")
                    .and(org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.ASC, "id"));
            case "activo" -> org.springframework.data.domain.Sort.by(sortDirection, "activo")
                    .and(org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.ASC, "id"));
            case "createdat", "created_at" -> org.springframework.data.domain.Sort.by(sortDirection, "createdAt")
                    .and(org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.ASC, "id"));
            case "updatedat", "updated_at" -> org.springframework.data.domain.Sort.by(sortDirection, "updatedAt")
                    .and(org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.ASC, "id"));
            default -> org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.ASC, "sku")
                    .and(org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.ASC, "id"));
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

    private double nz(Double value) {
        return value == null ? 0.0 : value;
    }

    private String generarSkuProducto(ModeloModel modelo, NivelModel nivel, MaterialModel material, ColorModel color) {
        if (modelo.getFamilia() == null || modelo.getFamilia().getLinea() == null) {
            throw new ValidationException("El modelo debe tener familia y linea para generar el sku");
        }

        String lineaCodigo = modelo.getFamilia().getLinea().getCodigo();
        String familiaCodigo = modelo.getFamilia().getCodigo();
        String modeloCodigo = modelo.getCodigo();
        String nivelCodigo = nivel.getCodigo();
        String materialCodigo = material.getCodigo();
        String colorCodigo = color.getCodigo();

        if (lineaCodigo == null || familiaCodigo == null || modeloCodigo == null || nivelCodigo == null
                || materialCodigo == null || colorCodigo == null) {
            throw new ValidationException("Faltan codigos requeridos para generar el sku del producto");
        }

        return (lineaCodigo + familiaCodigo + modeloCodigo + "-" + nivelCodigo + "-" + materialCodigo + "-" + colorCodigo).toUpperCase();
    }

    private boolean esProductoVisible(ProductoModel producto) {
        return producto.getModelo() != null || producto.getNivel() != null || producto.getMaterial() != null || producto.getColor() != null;
    }
}
