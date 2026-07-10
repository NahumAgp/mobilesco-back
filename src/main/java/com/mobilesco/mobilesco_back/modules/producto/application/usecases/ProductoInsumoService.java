package com.mobilesco.mobilesco_back.modules.producto.application.usecases;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mobilesco.mobilesco_back.dto.common.PageResponseDTO;
import com.mobilesco.mobilesco_back.modules.producto.infrastructure.in.api.dtos.ProductoInsumoCreateDTO;
import com.mobilesco.mobilesco_back.modules.producto.infrastructure.in.api.dtos.ProductoInsumoResponseDTO;
import com.mobilesco.mobilesco_back.modules.shared.application.exceptions.ResourceNotFoundException;
import com.mobilesco.mobilesco_back.modules.shared.application.exceptions.ValidationException;
import com.mobilesco.mobilesco_back.modules.insumo.domain.models.InsumoModel;
import com.mobilesco.mobilesco_back.modules.producto.domain.models.ProductoInsumoModel;
import com.mobilesco.mobilesco_back.modules.producto.domain.models.ProductoModel;
import com.mobilesco.mobilesco_back.modules.insumo.infrastructure.out.persistence.repositories.InsumoRepository;
import com.mobilesco.mobilesco_back.modules.producto.infrastructure.out.persistence.repositories.ProductoInsumoRepository;
import com.mobilesco.mobilesco_back.modules.producto.infrastructure.out.persistence.repositories.ProductoRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductoInsumoService {

    private final ProductoInsumoRepository productoInsumoRepository;
    private final ProductoRepository productoRepository;
    private final InsumoRepository insumoRepository;

    @Transactional
    public ProductoInsumoResponseDTO agregarInsumoAProducto(
            Long productoId, 
            ProductoInsumoCreateDTO dto) {
        
        log.info("Agregando insumo a producto - Producto ID: {}, Insumo ID: {}", 
                 productoId, dto.getInsumoId());
        
        ProductoModel producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));
        
        InsumoModel insumo = insumoRepository.findById(dto.getInsumoId())
                .orElseThrow(() -> new ResourceNotFoundException("Insumo no encontrado"));
        
        // Verificar si ya existe
        boolean existe = producto.getInsumos().stream()
                .anyMatch(i -> i.getInsumo().getId().equals(dto.getInsumoId()));
        
        if (existe) {
            throw new ValidationException("El insumo ya está agregado a este producto");
        }
        
        ProductoInsumoModel productoInsumo = ProductoInsumoModel.builder()
                .producto(producto)
                .insumo(insumo)
                .cantidad(dto.getCantidad())
                .desperdicioPorcentaje(dto.getDesperdicioPorcentaje() != null ? 
                                       dto.getDesperdicioPorcentaje() : 0.0)
                .observaciones(dto.getObservaciones())
                .build();
        
        ProductoInsumoModel saved = productoInsumoRepository.save(productoInsumo);
        log.info("Insumo agregado correctamente");
        
        return mapToResponseDTO(saved);
    }

    @Transactional(readOnly = true)
    public List<ProductoInsumoResponseDTO> listarPorProducto(Long productoId) {
        return productoInsumoRepository.findByProductoId(productoId)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PageResponseDTO<ProductoInsumoResponseDTO> listarPorProductoPaginado(Long productoId, Pageable pageable) {
        Page<ProductoInsumoResponseDTO> page = productoInsumoRepository.findByProductoId(productoId, pageable)
                .map(this::mapToResponseDTO);

        return new PageResponseDTO<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }

    @Transactional
    public void eliminarInsumoDeProducto(Long productoId, Long insumoId) {
        log.info("Eliminando insumo de producto - Producto ID: {}, Insumo ID: {}", 
                 productoId, insumoId);
        
        List<ProductoInsumoModel> items = productoInsumoRepository.findByProductoId(productoId);
        
        ProductoInsumoModel item = items.stream()
                .filter(i -> i.getInsumo().getId().equals(insumoId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "El insumo no está asociado a este producto"));
        
        productoInsumoRepository.delete(item);
        log.info("Insumo eliminado correctamente");
    }

    @Transactional
    public List<ProductoInsumoResponseDTO> agregarInsumosMasivo(
            Long productoId,
            List<ProductoInsumoCreateDTO> insumosDTO) {
        
        log.info("Agregando {} insumos al producto ID: {}", insumosDTO.size(), productoId);
        
        ProductoModel producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));
        
        List<ProductoInsumoResponseDTO> resultados = new ArrayList<>();
        List<String> errores = new ArrayList<>();
        
        for (ProductoInsumoCreateDTO dto : insumosDTO) {
            try {
                // Validar insumo existente
                InsumoModel insumo = insumoRepository.findById(dto.getInsumoId())
                        .orElseThrow(() -> new ResourceNotFoundException("Insumo no encontrado con id: " + dto.getInsumoId()));
                
                // Verificar si ya existe
                boolean existe = producto.getInsumos().stream()
                        .anyMatch(i -> i.getInsumo().getId().equals(dto.getInsumoId()));
                
                if (existe) {
                    errores.add("El insumo '" + insumo.getNombre() + "' ya está agregado");
                    continue;
                }
                
                // Crear nuevo
                ProductoInsumoModel productoInsumo = ProductoInsumoModel.builder()
                        .producto(producto)
                        .insumo(insumo)
                        .cantidad(dto.getCantidad())
                        .desperdicioPorcentaje(dto.getDesperdicioPorcentaje() != null ? 
                                               dto.getDesperdicioPorcentaje() : 0.0)
                        .observaciones(dto.getObservaciones())
                        .build();
                
                ProductoInsumoModel saved = productoInsumoRepository.save(productoInsumo);
                resultados.add(mapToResponseDTO(saved));
                
            } catch (Exception e) {
                errores.add("Error con insumo ID " + dto.getInsumoId() + ": " + e.getMessage());
            }
        }
        
        if (!errores.isEmpty()) {
            log.warn("Se completó con errores: {}", errores);
            if (resultados.isEmpty()) {
                throw new ValidationException("No se pudo agregar ningún insumo: " + errores);
            }
        }
        
        log.info("Se agregaron {} insumos correctamente", resultados.size());
        return resultados;
    }

    // =====================================================
    // 🔴 NUEVO MÉTODO: ACTUALIZAR INSUMO
    // =====================================================
    
    @Transactional
    public ProductoInsumoResponseDTO actualizarInsumo(
            Long productoId, 
            Long insumoId, 
            ProductoInsumoCreateDTO dto) {
        
        log.info("Actualizando insumo de producto - Producto ID: {}, Insumo ID: {}", 
                 productoId, insumoId);
        
        // Buscar la relación producto-insumo
        List<ProductoInsumoModel> items = productoInsumoRepository.findByProductoId(productoId);
        
        ProductoInsumoModel item = items.stream()
                .filter(i -> i.getInsumo().getId().equals(insumoId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "El insumo no está asociado a este producto"));
        
        // Actualizar campos
        if (dto.getCantidad() != null) {
            item.setCantidad(dto.getCantidad());
        }
        
        if (dto.getDesperdicioPorcentaje() != null) {
            item.setDesperdicioPorcentaje(dto.getDesperdicioPorcentaje());
        }
        
        if (dto.getObservaciones() != null) {
            item.setObservaciones(dto.getObservaciones());
        }
        
        ProductoInsumoModel updated = productoInsumoRepository.save(item);
        log.info("Insumo actualizado correctamente");
        
        return mapToResponseDTO(updated);
    }

    // =====================================================
    // MÉTODO PRIVADO DE MAPEO
    // =====================================================
    
    private ProductoInsumoResponseDTO mapToResponseDTO(ProductoInsumoModel pi) {
    // 🔴 Obtener costo del Kardex
    Double costoUnitario = obtenerCostoCotizacionOpcional(pi.getInsumo());
    
    // Calcular cantidad con desperdicio
    Double cantidadConDesperdicio = pi.getCantidad() * (1 + (pi.getDesperdicioPorcentaje() != null ? pi.getDesperdicioPorcentaje() : 0) / 100);
    
    // Calcular subtotal
    Double subtotal = cantidadConDesperdicio * costoUnitario;
    
    return ProductoInsumoResponseDTO.builder()
            .id(pi.getId())
            .insumoId(pi.getInsumo().getId())
            .insumoNombre(pi.getInsumo().getNombre())
            .insumoUnidad(pi.getInsumo().getUnidadMedida().getSimbolo())
            .cantidad(pi.getCantidad())
            .desperdicioPorcentaje(pi.getDesperdicioPorcentaje())
            .cantidadConDesperdicio(cantidadConDesperdicio)
            .costoUnitario(costoUnitario)              
            .subtotal(subtotal)                          
            .observaciones(pi.getObservaciones())
            .fechaRegistro(pi.getFechaRegistro())
            .fechaActualizacion(pi.getFechaActualizacion())
            .build();
}

private Double obtenerCostoCotizacionOpcional(InsumoModel insumo) {
    return insumo != null && insumo.getCostoCotizacion() != null ? insumo.getCostoCotizacion() : 0.0;
}
}
