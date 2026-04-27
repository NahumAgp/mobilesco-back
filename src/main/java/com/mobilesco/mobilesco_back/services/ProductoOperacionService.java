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
        
        Long productoIdValidado = validarProductoId(productoId);

        if (dtoList == null || dtoList.isEmpty()) {
            throw new ValidationException("Debe proporcionar al menos una operación");
        }

        log.info("Agregando {} operaciones al producto ID: {}", dtoList.size(), productoIdValidado);
        
        ProductoModel producto = productoRepository.findById(productoIdValidado)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));

        List<ProductoOperacionModel> operacionesAGuardar = dtoList.stream()
                .map(dto -> {
                    if (dto == null) {
                        throw new ValidationException("La operación no puede ser nula");
                    }

                    Long operacionId = dto.getOperacionId();
                    if (operacionId == null) {
                        throw new ValidationException("El ID de la operación es requerido");
                    }

                    Integer cantidadDto = dto.getCantidad();
                    if (cantidadDto == null) {
                        throw new ValidationException("La cantidad es requerida");
                    }
                    Integer cantidad = cantidadDto;
                    if (cantidad < 1) {
                        throw new ValidationException("La cantidad mínima es 1");
                    }

                    Integer ordenDto = dto.getOrden();
                    Integer orden = ordenDto != null ? ordenDto : 0;

                    OperacionModel operacion = operacionRepository.findById(operacionId)
                            .orElseThrow(() -> new ResourceNotFoundException(
                                "Operación no encontrada con ID: " + operacionId));

                    if (productoOperacionRepository.existsByProductoIdAndOperacionId(productoIdValidado, operacionId)) {
                        throw new ValidationException(
                            "La operación " + operacion.getNombre() + " ya existe en el producto");
                    }

                    ProductoOperacionModel nuevaOperacion = ProductoOperacionModel.builder()
                            .producto(producto)
                            .operacion(operacion)
                            .cantidad(cantidad)
                            .orden(orden)
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
        Long productoIdValidado = validarProductoId(productoId);

        return productoOperacionRepository.findByProductoIdOrderByOrdenAsc(productoIdValidado)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void eliminarOperacionDeProducto(Long productoId, Long operacionId) {
        Long productoIdValidado = validarProductoId(productoId);
        Long operacionIdValidado = validarOperacionId(operacionId);

        log.info("Eliminando operación ID: {} del producto ID: {}", operacionIdValidado, productoIdValidado);
        
        ProductoOperacionModel po = productoOperacionRepository
                .findByProductoIdAndOperacionId(productoIdValidado, operacionIdValidado)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Operación no encontrada en el producto"));

        productoOperacionRepository.delete(po);
        log.info("Operación eliminada correctamente");
    }

    @Transactional
    public void reordenarOperaciones(Long productoId, List<Long> operacionesIdsEnOrden) {
        Long productoIdValidado = validarProductoId(productoId);
        if (operacionesIdsEnOrden == null) {
            throw new ValidationException("Debe proporcionar el orden de las operaciones");
        }

        log.info("Reordenando operaciones del producto ID: {}", productoIdValidado);
        
        List<ProductoOperacionModel> operaciones = productoOperacionRepository
                .findByProductoIdOrderByOrdenAsc(productoIdValidado);

        java.util.Map<Long, ProductoOperacionModel> mapaOperaciones = operaciones.stream()
                .collect(java.util.stream.Collectors.toMap(
                    po -> po.getOperacion().getId(), 
                    po -> po
                ));

        for (int i = 0; i < operacionesIdsEnOrden.size(); i++) {
            Long operacionId = validarOperacionId(operacionesIdsEnOrden.get(i));
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
        Long productoIdValidado = validarProductoId(productoId);

        return productoOperacionRepository.findByProductoIdOrderByOrdenAsc(productoIdValidado)
                .stream()
                .map(ProductoOperacionModel::getImporteActividad)
                .mapToDouble(importe -> importe != null ? importe : 0.0)
                .sum();
    }

    private Long validarProductoId(Long productoId) {
        if (productoId == null) {
            throw new ValidationException("El ID del producto es requerido");
        }
        return productoId;
    }

    private Long validarOperacionId(Long operacionId) {
        if (operacionId == null) {
            throw new ValidationException("El ID de la operación es requerido");
        }
        return operacionId;
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
