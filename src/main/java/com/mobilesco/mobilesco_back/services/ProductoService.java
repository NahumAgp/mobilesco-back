package com.mobilesco.mobilesco_back.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mobilesco.mobilesco_back.dto.Producto.ProductoCreateDTO;
import com.mobilesco.mobilesco_back.dto.Producto.ProductoInsumoResponseDTO;
import com.mobilesco.mobilesco_back.dto.Producto.ProductoResponseDTO;
import com.mobilesco.mobilesco_back.dto.Producto.ProductoUpdateDTO;
import com.mobilesco.mobilesco_back.dto.ProductoOperacion.ProductoOperacionResponseDTO;
import com.mobilesco.mobilesco_back.exceptions.ResourceNotFoundException;
import com.mobilesco.mobilesco_back.exceptions.ValidationException;
import com.mobilesco.mobilesco_back.models.CategoriaModel;
import com.mobilesco.mobilesco_back.models.LineaProductoModel;
import com.mobilesco.mobilesco_back.models.MaterialModel;
import com.mobilesco.mobilesco_back.models.ProductoInsumoModel;
import com.mobilesco.mobilesco_back.models.ProductoModel;
import com.mobilesco.mobilesco_back.models.TipoProductoModel;
import com.mobilesco.mobilesco_back.repositories.CategoriaRepository;
import com.mobilesco.mobilesco_back.repositories.LineaProductoRepository;
import com.mobilesco.mobilesco_back.repositories.MaterialRepository;
import com.mobilesco.mobilesco_back.repositories.ProductoInsumoRepository;
import com.mobilesco.mobilesco_back.repositories.ProductoOperacionRepository;
import com.mobilesco.mobilesco_back.repositories.ProductoRepository;
import com.mobilesco.mobilesco_back.repositories.TipoProductoRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final TipoProductoRepository tipoProductoRepository;
    private final LineaProductoRepository lineaProductoRepository;
    private final CategoriaRepository categoriaRepository;
    private final MaterialRepository materialRepository;
    private final ProductoInsumoRepository productoInsumoRepository;
    private final ProductoOperacionRepository productoOperacionRepository;
    private final KardexService kardexService;
    @Transactional
    public ProductoResponseDTO crear(ProductoCreateDTO dto) {
        log.info("Creando nuevo producto con SKU: {}", dto.getSku());

        // Validar SKU único
        if (productoRepository.existsBySkuIgnoreCase(dto.getSku())) {
            throw new ValidationException("Ya existe un producto con SKU: " + dto.getSku());
        }

        // Validar tipo de producto
        TipoProductoModel tipoProducto = tipoProductoRepository.findById(dto.getTipoProductoId())
                .orElseThrow(() -> new ResourceNotFoundException("Tipo de producto no encontrado"));

        // Validar línea (opcional)
        LineaProductoModel linea = null;
        if (dto.getLineaId() != null) {
            linea = lineaProductoRepository.findById(dto.getLineaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Línea no encontrada"));
        }

        // Validar categoría (opcional)
        CategoriaModel categoria = null;
        if (dto.getCategoriaId() != null) {
            categoria = categoriaRepository.findById(dto.getCategoriaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada"));
        }

        // Validar material (opcional)
        MaterialModel material = null;
        if (dto.getMaterialId() != null) {
            material = materialRepository.findById(dto.getMaterialId())
                    .orElseThrow(() -> new ResourceNotFoundException("Material no encontrado"));
        }

        // Crear producto
        ProductoModel producto = ProductoModel.builder()
                .sku(dto.getSku())
                .nombre(dto.getNombre())
                .descripcion(dto.getDescripcion())
                .tipoProducto(tipoProducto)
                .linea(linea)
                .categoria(categoria)
                .material(material)
                .caracteristicas(dto.getCaracteristicas())
                .dimensiones(dto.getDimensiones())
                .pesoKg(dto.getPesoKg())
                .activo(true)
                .build();

        ProductoModel saved = productoRepository.save(producto);
        log.info("Producto creado con ID: {}", saved.getId());

        return mapToResponseDTO(saved);
    }

    @Transactional
    public ProductoResponseDTO actualizar(Long id, ProductoUpdateDTO dto) {
        log.info("Actualizando producto ID: {}", id);

        ProductoModel producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));

        // Validar SKU único (excepto si es el mismo)
        if (!producto.getSku().equalsIgnoreCase(dto.getSku()) &&
                productoRepository.existsBySkuIgnoreCase(dto.getSku())) {
            throw new ValidationException("Ya existe un producto con SKU: " + dto.getSku());
        }

        // Actualizar relaciones si cambiaron
        if (!producto.getTipoProducto().getId().equals(dto.getTipoProductoId())) {
            TipoProductoModel tipoProducto = tipoProductoRepository.findById(dto.getTipoProductoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Tipo de producto no encontrado"));
            producto.setTipoProducto(tipoProducto);
        }

        if (dto.getLineaId() != null) {
            if (producto.getLinea() == null || !producto.getLinea().getId().equals(dto.getLineaId())) {
                LineaProductoModel linea = lineaProductoRepository.findById(dto.getLineaId())
                        .orElseThrow(() -> new ResourceNotFoundException("Línea no encontrada"));
                producto.setLinea(linea);
            }
        } else {
            producto.setLinea(null);
        }

        if (dto.getCategoriaId() != null) {
            if (producto.getCategoria() == null || !producto.getCategoria().getId().equals(dto.getCategoriaId())) {
                CategoriaModel categoria = categoriaRepository.findById(dto.getCategoriaId())
                        .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada"));
                producto.setCategoria(categoria);
            }
        } else {
            producto.setCategoria(null);
        }

        if (dto.getMaterialId() != null) {
            if (producto.getMaterial() == null || !producto.getMaterial().getId().equals(dto.getMaterialId())) {
                MaterialModel material = materialRepository.findById(dto.getMaterialId())
                        .orElseThrow(() -> new ResourceNotFoundException("Material no encontrado"));
                producto.setMaterial(material);
            }
        } else {
            producto.setMaterial(null);
        }

        // Actualizar campos
        producto.setSku(dto.getSku());
        producto.setNombre(dto.getNombre());
        producto.setDescripcion(dto.getDescripcion());
        producto.setCaracteristicas(dto.getCaracteristicas());
        producto.setDimensiones(dto.getDimensiones());
        producto.setPesoKg(dto.getPesoKg());
        
        if (dto.getActivo() != null) {
            producto.setActivo(dto.getActivo());
        }

        ProductoModel updated = productoRepository.save(producto);
        log.info("Producto actualizado");

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
        log.info("Eliminando producto ID: {}", id);

        ProductoModel producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));

        producto.setActivo(false);
        productoRepository.save(producto);
        log.info("Producto desactivado");
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
    // Obtener insumos (código existente)
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

    // 🔴 NUEVO: Obtener operaciones
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

    return ProductoResponseDTO.builder()
            .id(producto.getId())
            .sku(producto.getSku())
            .nombre(producto.getNombre())
            .descripcion(producto.getDescripcion())
            .tipoProductoId(producto.getTipoProducto().getId())
            .tipoProductoNombre(producto.getTipoProducto().getNombre())
            .lineaId(producto.getLinea() != null ? producto.getLinea().getId() : null)
            .lineaNombre(producto.getLinea() != null ? producto.getLinea().getNombre() : null)
            .categoriaId(producto.getCategoria() != null ? producto.getCategoria().getId() : null)
            .categoriaNombre(producto.getCategoria() != null ? producto.getCategoria().getNombre() : null)
            .materialId(producto.getMaterial() != null ? producto.getMaterial().getId() : null)
            .materialNombre(producto.getMaterial() != null ? producto.getMaterial().getNombre() : null)
            .caracteristicas(producto.getCaracteristicas())
            .dimensiones(producto.getDimensiones())
            .pesoKg(producto.getPesoKg())
            .activo(producto.getActivo())
            .fechaRegistro(producto.getFechaRegistro())
            .fechaActualizacion(producto.getFechaActualizacion())
            .insumos(insumos)
            .operaciones(operaciones)  // 🔴 NUEVO
            .build();
}

     
}