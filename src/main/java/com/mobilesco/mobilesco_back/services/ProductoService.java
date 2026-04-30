package com.mobilesco.mobilesco_back.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mobilesco.mobilesco_back.dto.Producto.ProductoCreateDTO;
import com.mobilesco.mobilesco_back.dto.Producto.ProductoInsumoResponseDTO;
import com.mobilesco.mobilesco_back.dto.Producto.ProductoResponseDTO;
import com.mobilesco.mobilesco_back.dto.Producto.ProductoUpdateDTO;
import com.mobilesco.mobilesco_back.dto.imagen.ImagenResponseDTO;
import com.mobilesco.mobilesco_back.dto.ProductoOperacion.ProductoOperacionResponseDTO;
import com.mobilesco.mobilesco_back.exceptions.ResourceNotFoundException;
import com.mobilesco.mobilesco_back.exceptions.ValidationException;
import com.mobilesco.mobilesco_back.models.ColorModel;
import com.mobilesco.mobilesco_back.models.NivelModel;
import com.mobilesco.mobilesco_back.models.ProductoInsumoModel;
import com.mobilesco.mobilesco_back.models.ProductoModel;
import com.mobilesco.mobilesco_back.repositories.ColorRepository;
import com.mobilesco.mobilesco_back.models.ModeloModel;
import com.mobilesco.mobilesco_back.repositories.NivelRepository;
import com.mobilesco.mobilesco_back.repositories.ModeloRepository;
import com.mobilesco.mobilesco_back.repositories.ProductoInsumoRepository;
import com.mobilesco.mobilesco_back.repositories.ProductoOperacionRepository;
import com.mobilesco.mobilesco_back.repositories.ProductoRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductoService {

    private final ProductoRepository productoRepository;
    
    private final ModeloRepository modeloRepository;
    private final NivelRepository nivelRepository;
    private final ColorRepository colorRepository;
    private final ProductoInsumoRepository productoInsumoRepository;
    private final ProductoOperacionRepository productoOperacionRepository;
    private final KardexService kardexService;
    private final ImagenService imagenService;
    @Transactional
    public ProductoResponseDTO crear(ProductoCreateDTO dto) {
        ModeloModel modelo = modeloRepository.findById(dto.getModeloId())
                .orElseThrow(() -> new ResourceNotFoundException("Modelo no encontrado"));

        NivelModel nivel = nivelRepository.findById(dto.getNivelId())
                .orElseThrow(() -> new ResourceNotFoundException("Nivel no encontrado"));

        ColorModel color = colorRepository.findById(dto.getColorId())
                .orElseThrow(() -> new ResourceNotFoundException("Color no encontrado"));

        String skuGenerado = generarSkuProducto(modelo, nivel, color);
        if (productoRepository.existsBySkuIgnoreCase(skuGenerado)) {
            throw new ValidationException("Ya existe un producto con SKU: " + skuGenerado);
        }

        ProductoModel producto = ProductoModel.builder()
                .sku(skuGenerado)
                .nombre(dto.getNombre())
                .descripcion(dto.getDescripcion())
                .modelo(modelo)
                .nivel(nivel)
                .color(color)
                .activo(dto.getActivo() != null ? dto.getActivo() : true)
                .build();

        ProductoModel saved = productoRepository.save(producto);
        return mapToResponseDTO(saved);
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

    @Transactional(readOnly = true)
    public Double calcularCostoProducto(Long productoId) {
        List<ProductoInsumoModel> insumos = productoInsumoRepository.findByProductoId(productoId);
        
        return insumos.stream()
                .mapToDouble(pi -> {
                    Double costoUnitario = kardexService.calcularCostoPromedio(pi.getInsumo().getId());
                    return pi.getCantidad() * costoUnitario;
                })
                .sum();
    }

    @Transactional(readOnly = true)
    public Double calcularCostoProductoConDesperdicio(Long productoId) {
        List<ProductoInsumoModel> insumos = productoInsumoRepository.findByProductoId(productoId);
        
        return insumos.stream()
                .mapToDouble(pi -> {
                    Double costoUnitario = kardexService.calcularCostoPromedio(pi.getInsumo().getId());
                    double cantidadConDesperdicio = pi.getCantidad() * (1 + pi.getDesperdicioPorcentaje() / 100);
                    return cantidadConDesperdicio * costoUnitario;
                })
                .sum();
    }

    private ProductoResponseDTO mapToResponseDTO(ProductoModel producto) {
        List<ProductoInsumoResponseDTO> insumos = productoInsumoRepository.findByProductoId(producto.getId())
            .stream()
            .map(pi -> ProductoInsumoResponseDTO.builder()
                    .id(pi.getId())
                    .insumoId(pi.getInsumo().getId())
                    .insumoNombre(pi.getInsumo().getNombre())
                    .insumoUnidad(pi.getInsumo().getUnidadMedida().getSimbolo())
                    .cantidad(pi.getCantidad())
                    .desperdicioPorcentaje(pi.getDesperdicioPorcentaje())
                    .cantidadConDesperdicio(pi.getCantidad() * (1 + pi.getDesperdicioPorcentaje() / 100))
                    .observaciones(pi.getObservaciones())
                    .build())
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

        return ProductoResponseDTO.builder()
            .id(producto.getId())
            .sku(producto.getSku())
            .nombre(producto.getNombre())
            .descripcion(producto.getDescripcion())
            .modeloId(producto.getModelo() != null ? producto.getModelo().getId() : null)
            .modeloNombre(producto.getModelo() != null ? producto.getModelo().getNombre() : null)
            .nivelId(producto.getNivel() != null ? producto.getNivel().getId() : null)
            .nivelNombre(producto.getNivel() != null ? producto.getNivel().getNombre() : null)
            .colorId(producto.getColor() != null ? producto.getColor().getId() : null)
            .colorNombre(producto.getColor() != null ? producto.getColor().getNombre() : null)
            .activo(producto.getActivo())
            .createdAt(producto.getCreatedAt())
            .updatedAt(producto.getUpdatedAt())
            .imagenPrincipal(imagenService.obtenerPrincipalPorProducto(producto.getId()))
            .imagenes(imagenes)
            .insumos(insumos)
            .operaciones(operaciones)
            .build();
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
        return productoRepository.buscarConFiltros(sku, nombre, modeloId, nivelId, colorId)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    public void eliminarProducto(Long id) {
        if (!productoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Producto no encontrado");
        }
        productoRepository.deleteById(id);
    }

    private String generarSkuProducto(ModeloModel modelo, NivelModel nivel, ColorModel color) {
        if (modelo.getFamilia() == null || modelo.getFamilia().getLinea() == null) {
            throw new ValidationException("El modelo debe tener familia y linea para generar el sku");
        }

        String lineaCodigo = modelo.getFamilia().getLinea().getCodigo();
        String familiaCodigo = modelo.getFamilia().getCodigo();
        String modeloCodigo = modelo.getCodigo();
        String nivelCodigo = nivel.getCodigo();
        String colorCodigo = color.getCodigo();

        if (lineaCodigo == null || familiaCodigo == null || modeloCodigo == null || nivelCodigo == null || colorCodigo == null) {
            throw new ValidationException("Faltan codigos requeridos para generar el sku del producto");
        }

        return (lineaCodigo + familiaCodigo + modeloCodigo + "-" + nivelCodigo + "-" + colorCodigo).toUpperCase();
    }

    private boolean esProductoVisible(ProductoModel producto) {
        return producto.getModelo() != null || producto.getNivel() != null || producto.getColor() != null;
    }
}
