package com.mobilesco.mobilesco_back.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mobilesco.mobilesco_back.dto.ProductoOperacion.ProductoOperacionCreateDTO;
import com.mobilesco.mobilesco_back.dto.ProductoOperacion.ProductoOperacionResponseDTO;
import com.mobilesco.mobilesco_back.exceptions.ResourceNotFoundException;
import com.mobilesco.mobilesco_back.exceptions.ValidationException;
import com.mobilesco.mobilesco_back.models.OperacionModel;
import com.mobilesco.mobilesco_back.models.ProductoModel;
import com.mobilesco.mobilesco_back.models.ProductoOperacionModel;
import com.mobilesco.mobilesco_back.repositories.OperacionRepository;
import com.mobilesco.mobilesco_back.repositories.ProductoOperacionRepository;
import com.mobilesco.mobilesco_back.repositories.ProductoRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductoOperacionService {

    private final ProductoOperacionRepository productoOperacionRepository;
    private final ProductoRepository productoRepository;
    private final OperacionRepository operacionRepository;

    @Transactional
    public List<ProductoOperacionResponseDTO> agregarOperacionesMasivo(
            Long productoId, 
            List<ProductoOperacionCreateDTO> dtoList) {
        
        log.info("Agregando {} operaciones al producto ID: {}", dtoList.size(), productoId);
        
        ProductoModel producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));

        List<ProductoOperacionModel> operacionesAGuardar = dtoList.stream()
                .map(dto -> {
                    OperacionModel operacion = operacionRepository.findById(dto.getOperacionId())
                            .orElseThrow(() -> new ResourceNotFoundException(
                                "Operación no encontrada con ID: " + dto.getOperacionId()));

                    if (productoOperacionRepository.existsByProductoIdAndOperacionId(productoId, dto.getOperacionId())) {
                        throw new ValidationException(
                            "La operación " + operacion.getNombre() + " ya existe en el producto");
                    }

                    ProductoOperacionModel nuevaOperacion = ProductoOperacionModel.builder()
                            .producto(producto)
                            .operacion(operacion)
                            .cantidad(dto.getCantidad() != null ? dto.getCantidad() : 1)
                            .orden(dto.getOrden() != null ? dto.getOrden() : 0)
                            .observaciones(dto.getObservaciones())
                            .activo(true)
                            .build();
                    
                    // 🔴 ¡AQUÍ LLAMAS AL MÉTODO DE LA ENTIDAD!
                    nuevaOperacion.calcularTotales();  // ✅ Esto funciona
                    
                    return nuevaOperacion;
                })
                .collect(Collectors.toList());

        List<ProductoOperacionModel> saved = productoOperacionRepository.saveAll(operacionesAGuardar);
        log.info("{} operaciones agregadas correctamente", saved.size());

        return saved.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProductoOperacionResponseDTO> listarPorProducto(Long productoId) {
        return productoOperacionRepository.findByProductoIdOrderByOrdenAsc(productoId)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void eliminarOperacionDeProducto(Long productoId, Long operacionId) {
        log.info("Eliminando operación ID: {} del producto ID: {}", operacionId, productoId);
        
        ProductoOperacionModel po = productoOperacionRepository
                .findByProductoIdAndOperacionId(productoId, operacionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Operación no encontrada en el producto"));

        productoOperacionRepository.delete(po);
        log.info("Operación eliminada correctamente");
    }

    @Transactional
public void reordenarOperaciones(Long productoId, List<Long> operacionesIdsEnOrden) {
    log.info("Reordenando operaciones del producto ID: {}", productoId);
    
    List<ProductoOperacionModel> operaciones = productoOperacionRepository
            .findByProductoIdOrderByOrdenAsc(productoId);

    // Crear un mapa para búsqueda rápida
    java.util.Map<Long, ProductoOperacionModel> mapaOperaciones = operaciones.stream()
            .collect(java.util.stream.Collectors.toMap(
                po -> po.getOperacion().getId(), 
                po -> po
            ));

    // Actualizar órdenes según la lista recibida
    for (int i = 0; i < operacionesIdsEnOrden.size(); i++) {
        Long operacionId = operacionesIdsEnOrden.get(i);
        ProductoOperacionModel po = mapaOperaciones.get(operacionId);
        if (po != null) {
            po.setOrden(i + 1);
        }
    }

    productoOperacionRepository.saveAll(operaciones);
    log.info("Operaciones reordenadas correctamente");
}

    @Transactional(readOnly = true)
    public Double calcularCostoTotalOperaciones(Long productoId) {
        return productoOperacionRepository.findByProductoIdOrderByOrdenAsc(productoId)
                .stream()
                .mapToDouble(ProductoOperacionModel::getImporteActividad)
                .sum();
    }

    private ProductoOperacionResponseDTO mapToResponseDTO(ProductoOperacionModel po) {
        return ProductoOperacionResponseDTO.builder()
                .id(po.getId())
                .productoId(po.getProducto().getId())
                .productoSku(po.getProducto().getSku())
                .productoNombre(po.getProducto().getNombre())
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
                .activo(po.getActivo())
                .fechaRegistro(po.getFechaRegistro())
                .fechaActualizacion(po.getFechaActualizacion())
                .build();
    }
}