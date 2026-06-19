package com.mobilesco.mobilesco_back.modules.compra.application.usecases;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mobilesco.mobilesco_back.modules.compra.infrastructure.in.api.dtos.CompraCreateDTO;
import com.mobilesco.mobilesco_back.modules.compra.infrastructure.in.api.dtos.CompraResponseDTO;
import com.mobilesco.mobilesco_back.modules.compra.infrastructure.in.api.dtos.CompraUpdateDTO;
import com.mobilesco.mobilesco_back.modules.compra.infrastructure.in.api.dtos.DetalleCompraCreateDTO;
import com.mobilesco.mobilesco_back.modules.compra.infrastructure.in.api.dtos.DetalleCompraResponseDTO;
import com.mobilesco.mobilesco_back.modules.shared.application.exceptions.ResourceNotFoundException;
import com.mobilesco.mobilesco_back.modules.shared.application.exceptions.ValidationException;
import com.mobilesco.mobilesco_back.modules.compra.domain.models.CompraModel;
import com.mobilesco.mobilesco_back.modules.compra.domain.models.DetalleCompraModel;
import com.mobilesco.mobilesco_back.modules.insumo.domain.models.InsumoModel;
import com.mobilesco.mobilesco_back.modules.kardex.application.usecases.KardexService;
import com.mobilesco.mobilesco_back.modules.proveedor.domain.models.ProveedorModel;
import com.mobilesco.mobilesco_back.modules.compra.infrastructure.out.persistence.repositories.CompraRepository;
import com.mobilesco.mobilesco_back.modules.compra.infrastructure.out.persistence.repositories.DetalleCompraRepository;
import com.mobilesco.mobilesco_back.modules.insumo.infrastructure.out.persistence.repositories.InsumoRepository;
import com.mobilesco.mobilesco_back.modules.proveedor.infrastructure.out.persistence.repositories.ProveedorRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompraService {

    private final CompraRepository compraRepository;
    private final ProveedorRepository proveedorRepository;
    private final InsumoRepository insumoRepository;
    private final DetalleCompraRepository detalleCompraRepository;
    private final DetalleCompraService detalleCompraService;
    private final KardexService kardexService;

    /**
     * CREAR una nueva compra con sus detalles
     */
    @Transactional
    public CompraResponseDTO crear(CompraCreateDTO dto) {
        log.info("Creando nueva compra - Folio: {}, Proveedor ID: {}", dto.getFolio(), dto.getProveedorId());
        
        // Validar proveedor
        ProveedorModel proveedor = proveedorRepository.findById(dto.getProveedorId())
                .orElseThrow(() -> new ResourceNotFoundException("Proveedor no encontrado con id: " + dto.getProveedorId()));
        
        if (!proveedor.getActivo()) {
            throw new ValidationException("El proveedor está inactivo: " + proveedor.getNombre());
        }
        
        // Validar que haya detalles
        if (dto.getDetalles() == null || dto.getDetalles().isEmpty()) {
            throw new ValidationException("La compra debe tener al menos un detalle");
        }
        
        // Validar número de documento único si aplica
        if (dto.getNumeroDocumento() != null && !dto.getNumeroDocumento().isEmpty()) {
            if (compraRepository.existsByNumeroDocumento(dto.getNumeroDocumento())) {
                throw new ValidationException("Ya existe una compra con el documento: " + dto.getNumeroDocumento());
            }
        }
        
        // Crear compra
        CompraModel compra = CompraModel.builder()
                .folio(dto.getFolio())
                .fechaCompra(dto.getFechaCompra() != null ? dto.getFechaCompra() : LocalDate.now())
                .fechaRecepcion(dto.getFechaRecepcion())
                .proveedor(proveedor)
                .tipoDocumento(dto.getTipoDocumento())
                .numeroDocumento(dto.getNumeroDocumento())
                .subtotal(dto.getSubtotal())
                .impuesto(dto.getImpuesto())
                .total(dto.getTotal())
                .observaciones(dto.getObservaciones())
                .estado("PENDIENTE")
                .activo(true)
                .build();
        
        CompraModel savedCompra = compraRepository.save(compra);
        log.info("Compra creada con ID: {}", savedCompra.getId());
        actualizarUltimoContactoProveedor(savedCompra.getProveedor());
        
        // Procesar detalles
        double subtotalCalculado = 0;
        
        for (DetalleCompraCreateDTO detalleDTO : dto.getDetalles()) {
            try {
                DetalleCompraResponseDTO detalleCreado = detalleCompraService.crear(savedCompra.getId(), detalleDTO);
                subtotalCalculado += detalleCreado.getSubtotal();
            } catch (Exception e) {
                log.error("Error al crear detalle: {}", e.getMessage());
                throw new ValidationException("Error al crear detalle: " + e.getMessage());
            }
        }
        
        // Actualizar totales si no fueron proporcionados
        boolean actualizar = false;
        
        if (dto.getSubtotal() == null) {
            savedCompra.setSubtotal(subtotalCalculado);
            actualizar = true;
        }
        
        if (dto.getTotal() == null) {
            
            Double impuestoValue = dto.getImpuesto();
            double impuesto = impuestoValue != null ? impuestoValue : 0.0;
            savedCompra.setTotal(subtotalCalculado + impuesto);
            actualizar = true;
        }
        
        if (actualizar) {
            compraRepository.save(savedCompra);
        }
        
        return mapToResponseDTO(savedCompra);
    }

    /**
     * ACTUALIZAR una compra (solo datos generales, no detalles)
     */
    @Transactional
    public CompraResponseDTO actualizar(Long id, CompraUpdateDTO dto) {
        log.info("Actualizando compra ID: {}", id);
        
        CompraModel compra = compraRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Compra no encontrada con id: " + id));
        
        // Validar que no esté cancelada o ya recibida
        if ("CANCELADA".equals(compra.getEstado())) {
            throw new ValidationException("No se puede actualizar una compra cancelada");
        }
        
        if (("RECIBIDA".equals(compra.getEstado()) || "RECIBIDA_PARCIAL".equals(compra.getEstado())) && dto.getEstado() == null) {
            throw new ValidationException("No se puede actualizar una compra ya recibida");
        }
        
        // Actualizar campos
        if (dto.getFolio() != null) compra.setFolio(dto.getFolio());
        if (dto.getFechaCompra() != null) compra.setFechaCompra(dto.getFechaCompra());
        if (dto.getFechaRecepcion() != null) compra.setFechaRecepcion(dto.getFechaRecepcion());
        
        if (dto.getProveedorId() != null) {
            ProveedorModel proveedor = proveedorRepository.findById(dto.getProveedorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Proveedor no encontrado"));
            compra.setProveedor(proveedor);
            actualizarUltimoContactoProveedor(proveedor);
        }
        
        if (dto.getTipoDocumento() != null) compra.setTipoDocumento(dto.getTipoDocumento());
        
        // Validar número de documento único si cambió
        if (dto.getNumeroDocumento() != null && !dto.getNumeroDocumento().equals(compra.getNumeroDocumento())) {
            if (compraRepository.existsByNumeroDocumento(dto.getNumeroDocumento())) {
                throw new ValidationException("Ya existe una compra con el documento: " + dto.getNumeroDocumento());
            }
            compra.setNumeroDocumento(dto.getNumeroDocumento());
        }
        
        if (dto.getSubtotal() != null) compra.setSubtotal(dto.getSubtotal());
        if (dto.getImpuesto() != null) compra.setImpuesto(dto.getImpuesto());
        if (dto.getTotal() != null) compra.setTotal(dto.getTotal());
        if (dto.getObservaciones() != null) compra.setObservaciones(dto.getObservaciones());
        if (dto.getEntregadoPor() != null) compra.setEntregadoPor(dto.getEntregadoPor().trim());
        if (dto.getEstado() != null) compra.setEstado(dto.getEstado());
        if (dto.getActivo() != null) compra.setActivo(dto.getActivo());
        
        CompraModel updated = compraRepository.save(compra);
        log.info("Compra actualizada: {}", updated.getId());
        
        return mapToResponseDTO(updated);
    }

    /**
     * RECIBIR una compra (actualizar stock de insumos)
     */
    @Transactional
    public CompraResponseDTO recibirCompra(Long id) {
        log.info("Recibiendo compra ID: {}", id);
        
        CompraModel compra = compraRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Compra no encontrada con id: " + id));
        
        if ("RECIBIDA".equals(compra.getEstado())) {
            throw new ValidationException("La compra ya fue recibida");
        }
        
        if ("CANCELADA".equals(compra.getEstado())) {
            throw new ValidationException("No se puede recibir una compra cancelada");
        }
        
        // Obtener detalles
        List<DetalleCompraModel> detalles = detalleCompraRepository.findByCompraId(id);
        
        if (detalles.isEmpty()) {
            throw new ValidationException("La compra no tiene detalles");
        }
        
        // Actualizar stock de cada insumo
        for (DetalleCompraModel detalle : detalles) {
            InsumoModel insumo = detalle.getInsumo();
            
            double cantidadRecibidaAnterior = detalle.getCantidadRecibida() != null ? detalle.getCantidadRecibida() : 0.0;
            double cantidadPendiente = Math.max((detalle.getCantidad() != null ? detalle.getCantidad() : 0.0) - cantidadRecibidaAnterior, 0.0);
            if (cantidadPendiente <= 0) {
                continue;
            }

            double cantidadEnUnidadConsumo = cantidadPendiente * detalle.getFactorConversion();
            double stockAnterior = insumo.getStockActual();
            
            insumo.setStockActual(stockAnterior + cantidadEnUnidadConsumo);
            insumoRepository.save(insumo);
            
            log.info("Stock actualizado - Insumo: {}, Anterior: {}, Nuevo: {}, +{} {}", 
                    insumo.getNombre(), stockAnterior, insumo.getStockActual(), 
                    cantidadEnUnidadConsumo, insumo.getUnidadMedida().getSimbolo());
            
            // Aquí deberías registrar en Kardex
            kardexService.registrarEntradaCompra(
                    insumo.getId(),
                    cantidadEnUnidadConsumo,
                    detalle.getCostoPorUnidadConsumo(),
                    compra.getNumeroDocumento(),
                    compra.getId(),
                    "Entrada por compra: " + compra.getFolio()
            );

            detalle.setCantidadRecibida(detalle.getCantidad());
            detalle.setMotivoNoRecepcion(null);
            detalleCompraRepository.save(detalle);
        }
        
        compra.setEstado("RECIBIDA");
        compra.setFechaRecepcion(LocalDate.now());
        
        CompraModel updated = compraRepository.save(compra);
        log.info("Compra recibida exitosamente");
        actualizarUltimoContactoProveedor(compra.getProveedor());

        return mapToResponseDTO(updated);
    }

    /**
     * CANCELAR una compra
     */
    @Transactional
    public CompraResponseDTO cancelarCompra(Long id, String motivo) {
        log.info("Cancelando compra ID: {}", id);
        
        CompraModel compra = compraRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Compra no encontrada con id: " + id));
        
        if ("RECIBIDA".equals(compra.getEstado()) || "RECIBIDA_PARCIAL".equals(compra.getEstado())) {
            throw new ValidationException("No se puede cancelar una compra ya recibida");
        }
        
        compra.setEstado("CANCELADA");
        compra.setObservaciones(compra.getObservaciones() + " | CANCELADA: " + motivo);
        
        CompraModel updated = compraRepository.save(compra);
        log.info("Compra cancelada");
        
        return mapToResponseDTO(updated);
    }

    /**
     * OBTENER compra por ID
     */
    @Transactional(readOnly = true)
    public CompraResponseDTO obtenerPorId(Long id) {
        CompraModel compra = compraRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Compra no encontrada con id: " + id));
        return mapToResponseDTO(compra);
    }

    /**
     * LISTAR todas las compras
     */
    @Transactional(readOnly = true)
    public List<CompraResponseDTO> listar() {
        return compraRepository.findByActivoTrue()
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * LISTAR compras por proveedor
     */
    @Transactional(readOnly = true)
    public List<CompraResponseDTO> listarPorProveedor(Long proveedorId) {
        return compraRepository.findByProveedorIdAndActivoTrue(proveedorId)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * LISTAR compras por estado
     */
    @Transactional(readOnly = true)
    public List<CompraResponseDTO> listarPorEstado(String estado) {
        return compraRepository.findByEstadoAndActivoTrue(estado)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * LISTAR compras por rango de fechas
     */
    @Transactional(readOnly = true)
    public List<CompraResponseDTO> listarPorRangoFechas(LocalDate fechaInicio, LocalDate fechaFin) {
        return compraRepository.findByRangoFechas(fechaInicio, fechaFin)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * BUSCAR compras por folio
     */
    @Transactional(readOnly = true)
    public List<CompraResponseDTO> buscarPorFolio(String folio) {
        return compraRepository.buscarPorFolio(folio)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * ELIMINAR (desactivar) compra
     */
    @Transactional
    public void eliminar(Long id) {
        String usuario = obtenerUsuarioAutenticado();
        log.info("Eliminando (desactivando) compra ID: {} por usuario: {}", id, usuario);
        
        CompraModel compra = compraRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Compra no encontrada con id: " + id));
        
        if ("RECIBIDA".equals(compra.getEstado()) || "RECIBIDA_PARCIAL".equals(compra.getEstado())) {
            throw new ValidationException("No se puede eliminar una compra recibida");
        }
        
        compra.setActivo(false);
        compraRepository.save(compra);
        
        Long proveedorId = compra.getProveedor() != null ? compra.getProveedor().getId() : null;
        log.info(
                "Auditoria compra eliminada - id: {}, folio: {}, proveedorId: {}, estado: {}, usuario: {}",
                compra.getId(),
                compra.getFolio(),
                proveedorId,
                compra.getEstado(),
                usuario);
    }

    private String obtenerUsuarioAutenticado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            return "desconocido";
        }
        return authentication.getName();
    }

    private void actualizarUltimoContactoProveedor(ProveedorModel proveedor) {
        if (proveedor == null) {
            return;
        }

        proveedor.setFechaUltimoContacto(LocalDate.now());
        proveedorRepository.save(proveedor);
    }

    /**
     * Mapear de Entity a ResponseDTO
     */
   private CompraResponseDTO mapToResponseDTO(CompraModel compra) {
        ProveedorModel p = compra.getProveedor();
        
        // Construir nombre completo
        String nombreCompleto = p.getNombre() != null ? p.getNombre() : "";
        if (p.getApellidoPaterno() != null) {
            nombreCompleto += " " + p.getApellidoPaterno();
        }
        if (p.getApellidoMaterno() != null) {
            nombreCompleto += " " + p.getApellidoMaterno();
        }
        
        // 🔴 Obtener detalles directamente del repositorio
        List<DetalleCompraResponseDTO> detalles = detalleCompraRepository.findByCompraId(compra.getId())
                .stream()
                .map(d -> DetalleCompraResponseDTO.builder()
                        .id(d.getId())
                        .insumoId(d.getInsumo().getId())
                        .insumoNombre(d.getInsumo().getNombre())
                        .cantidad(d.getCantidad())
                        .factorConversion(d.getFactorConversion())
                        .cantidadRecibida(d.getCantidadRecibida())
                        .cantidadEnUnidadConsumo(d.getCantidadEnUnidadConsumo())
                        .cantidadPendiente(d.getCantidadPendiente())
                        .unidadCompraId(d.getUnidadCompra().getId())
                        .unidadCompraSimbolo(d.getUnidadCompra().getSimbolo())
                        .unidadConsumoId(d.getInsumo().getUnidadMedida().getId())
                        .unidadConsumoSimbolo(d.getInsumo().getUnidadMedida().getSimbolo())
                        .precioUnitario(d.getPrecioUnitario())
                        .costoPorUnidadConsumo(d.getCostoPorUnidadConsumo())
                        .subtotal(d.getSubtotal())
                        .observaciones(d.getObservaciones())
                        .motivoNoRecepcion(d.getMotivoNoRecepcion())
                        .build())
                .collect(Collectors.toList());
        
        return CompraResponseDTO.builder()
                .id(compra.getId())
                .folio(compra.getFolio())
                .fechaCompra(compra.getFechaCompra())
                .fechaRecepcion(compra.getFechaRecepcion())
                
                // Datos del proveedor
                .proveedorId(p.getId())
                .proveedorRazonSocial(p.getRazonSocial())
                .proveedorRfc(p.getRfc())
                .proveedorNombreCompleto(nombreCompleto.trim())
                .entregadoPor(compra.getEntregadoPor())
                
                .tipoDocumento(compra.getTipoDocumento())
                .numeroDocumento(compra.getNumeroDocumento())
                .subtotal(compra.getSubtotal())
                .impuesto(compra.getImpuesto())
                .total(compra.getTotal())
                .observaciones(compra.getObservaciones())
                .estado(compra.getEstado())
                .activo(compra.getActivo())
                .fechaRegistro(compra.getFechaRegistro())
                .fechaActualizacion(compra.getFechaActualizacion())
                .detalles(detalles)  // ✅ Ahora sí existe
                .build();
    }
}
