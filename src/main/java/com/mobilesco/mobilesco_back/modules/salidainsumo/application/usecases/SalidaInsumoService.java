package com.mobilesco.mobilesco_back.modules.salidainsumo.application.usecases;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mobilesco.mobilesco_back.modules.salidainsumo.infrastructure.in.api.dtos.DetalleSalidaInsumoCreateDTO;
import com.mobilesco.mobilesco_back.modules.salidainsumo.infrastructure.in.api.dtos.DetalleSalidaInsumoResponseDTO;
import com.mobilesco.mobilesco_back.modules.salidainsumo.infrastructure.in.api.dtos.SalidaInsumoCreateDTO;
import com.mobilesco.mobilesco_back.modules.salidainsumo.infrastructure.in.api.dtos.SalidaInsumoResponseDTO;
import com.mobilesco.mobilesco_back.modules.shared.application.exceptions.ResourceNotFoundException;
import com.mobilesco.mobilesco_back.modules.shared.application.exceptions.ValidationException;
import com.mobilesco.mobilesco_back.modules.salidainsumo.domain.models.DetalleSalidaInsumoModel;
import com.mobilesco.mobilesco_back.modules.insumo.domain.models.InsumoModel;
import com.mobilesco.mobilesco_back.modules.kardex.application.usecases.KardexService;
import com.mobilesco.mobilesco_back.modules.kardex.domain.models.MovimientoInsumoModel;
import com.mobilesco.mobilesco_back.modules.salidainsumo.domain.models.SalidaInsumoModel;
import com.mobilesco.mobilesco_back.modules.salidainsumo.infrastructure.out.persistence.repositories.DetalleSalidaInsumoRepository;
import com.mobilesco.mobilesco_back.modules.insumo.infrastructure.out.persistence.repositories.InsumoRepository;
import com.mobilesco.mobilesco_back.modules.kardex.infrastructure.out.persistence.repositories.KardexRepository;
import com.mobilesco.mobilesco_back.modules.salidainsumo.infrastructure.out.persistence.repositories.SalidaInsumoRepository;
import com.mobilesco.mobilesco_back.modules.auth.infrastructure.out.persistence.repositories.UsuarioRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SalidaInsumoService {

    private final SalidaInsumoRepository salidaInsumoRepository;
    private final DetalleSalidaInsumoRepository detalleSalidaInsumoRepository;
    private final InsumoRepository insumoRepository;
    private final KardexRepository kardexRepository;
    private final KardexService kardexService;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public SalidaInsumoResponseDTO crear(SalidaInsumoCreateDTO dto) {
        log.info("Creando salida de insumos para orden: {}", dto.getOrdenProduccion());

        if (dto.getDetalles() == null || dto.getDetalles().isEmpty()) {
            throw new ValidationException("La salida debe tener al menos un insumo");
        }

        SalidaInsumoModel salida = SalidaInsumoModel.builder()
                .ordenProduccion(dto.getOrdenProduccion().trim())
                .fechaSalida(dto.getFechaSalida() != null ? dto.getFechaSalida() : LocalDateTime.now())
                .observaciones(dto.getObservaciones())
                .responsable(dto.getResponsable() != null ? dto.getResponsable().trim() : null)
                .cantidadTotal(0.0)
                .activo(true)
                .usuario(obtenerUsuarioActual())
                .detalles(new ArrayList<>())
                .build();

        salida = salidaInsumoRepository.save(salida);

        double cantidadTotal = 0.0;
        List<DetalleSalidaInsumoModel> detallesGuardados = new ArrayList<>();

        for (DetalleSalidaInsumoCreateDTO detalleDTO : dto.getDetalles()) {
            DetalleSalidaInsumoModel detalle = procesarDetalle(salida, detalleDTO);
            detallesGuardados.add(detalle);
            cantidadTotal += detalle.getCantidad();
        }

        salida.setCantidadTotal(cantidadTotal);
        salida.setDetalles(detallesGuardados);
        salida = salidaInsumoRepository.save(salida);

        return mapToResponseDTO(salida);
    }

    @Transactional(readOnly = true)
    public List<SalidaInsumoResponseDTO> listar() {
        return salidaInsumoRepository.findAllByOrderByFechaSalidaDesc()
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SalidaInsumoResponseDTO obtenerPorId(Long id) {
        SalidaInsumoModel salida = salidaInsumoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Salida de insumos no encontrada con id: " + id));
        return mapToResponseDTO(salida);
    }

    private DetalleSalidaInsumoModel procesarDetalle(SalidaInsumoModel salida, DetalleSalidaInsumoCreateDTO dto) {
        InsumoModel insumo = insumoRepository.findById(dto.getInsumoId())
                .orElseThrow(() -> new ResourceNotFoundException("Insumo no encontrado con id: " + dto.getInsumoId()));

        Double cantidad = dto.getCantidad();
        Double stockAnterior = insumo.getStockActual() != null ? insumo.getStockActual() : 0.0;

        if (stockAnterior < cantidad) {
            throw new ValidationException(String.format(
                    "Stock insuficiente para %s. Disponible: %.2f %s, solicitado: %.2f %s",
                    insumo.getNombre(),
                    stockAnterior,
                    insumo.getUnidadMedida().getSimbolo(),
                    cantidad,
                    insumo.getUnidadMedida().getSimbolo()));
        }

        Double stockNuevo = stockAnterior - cantidad;
        Double costoUnitario = kardexService.calcularCostoPromedio(insumo.getId());
        if (costoUnitario == null) {
            costoUnitario = 0.0;
        }
        Double costoTotal = cantidad * costoUnitario;

        insumo.setStockActual(stockNuevo);
        insumoRepository.save(insumo);

        DetalleSalidaInsumoModel detalle = DetalleSalidaInsumoModel.builder()
                .salidaInsumo(salida)
                .insumo(insumo)
                .cantidad(cantidad)
                .stockAnterior(stockAnterior)
                .stockNuevo(stockNuevo)
                .costoUnitario(costoUnitario)
                .costoTotal(costoTotal)
                .observaciones(dto.getObservaciones())
                .build();

        DetalleSalidaInsumoModel savedDetalle = detalleSalidaInsumoRepository.save(detalle);

        MovimientoInsumoModel movimiento = MovimientoInsumoModel.builder()
                .insumo(insumo)
                .fecha(salida.getFechaSalida())
                .tipo("SALIDA")
                .concepto("PRODUCCION")
                .cantidad(cantidad)
                .costoUnitario(costoUnitario)
                .costoTotal(costoTotal)
                .documento(salida.getOrdenProduccion())
                .referencia("Salida #" + salida.getId())
                .observaciones(dto.getObservaciones())
                .stockAnterior(stockAnterior)
                .stockNuevo(stockNuevo)
                .usuario(obtenerUsuarioActual())
                .build();

        kardexRepository.save(movimiento);

        log.info("Salida registrada - Insumo: {}, Anterior: {}, Nuevo: {}, Cantidad: {}",
                insumo.getNombre(), stockAnterior, stockNuevo, cantidad);

        return savedDetalle;
    }

    private String obtenerUsuarioActual() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return null;
        }

        String email = authentication.getName();
        if (email == null || email.isBlank()) {
            return null;
        }

        return usuarioRepository.findOneByEmail(email)
                .map(usuario -> {
                    if (usuario.getEmpleado() == null) {
                        return usuario.getEmail();
                    }

                    var empleado = usuario.getEmpleado();
                    String nombreCompleto = String.join(" ",
                            empleado.getNombre(),
                            empleado.getApellidoPaterno()
                    ).trim().replaceAll("\\s+", " ");

                    return nombreCompleto.isBlank() ? usuario.getEmail() : nombreCompleto;
                })
                .orElse(email);
    }

    private SalidaInsumoResponseDTO mapToResponseDTO(SalidaInsumoModel salida) {
        List<DetalleSalidaInsumoResponseDTO> detalles = detalleSalidaInsumoRepository
                .findBySalidaInsumoIdOrderByIdAsc(salida.getId())
                .stream()
                .map(this::mapDetalleToResponseDTO)
                .collect(Collectors.toList());

        return SalidaInsumoResponseDTO.builder()
                .id(salida.getId())
                .ordenProduccion(salida.getOrdenProduccion())
                .fechaSalida(salida.getFechaSalida())
                .observaciones(salida.getObservaciones())
                .responsable(salida.getResponsable())
                .cantidadTotal(salida.getCantidadTotal())
                .activo(salida.getActivo())
                .usuario(salida.getUsuario())
                .fechaRegistro(salida.getFechaRegistro())
                .fechaActualizacion(salida.getFechaActualizacion())
                .detalles(detalles)
                .build();
    }

    private DetalleSalidaInsumoResponseDTO mapDetalleToResponseDTO(DetalleSalidaInsumoModel detalle) {
        return DetalleSalidaInsumoResponseDTO.builder()
                .id(detalle.getId())
                .salidaInsumoId(detalle.getSalidaInsumo().getId())
                .insumoId(detalle.getInsumo().getId())
                .insumoNombre(detalle.getInsumo().getNombre())
                .insumoUnidad(detalle.getInsumo().getUnidadMedida() != null
                        ? detalle.getInsumo().getUnidadMedida().getSimbolo()
                        : null)
                .cantidad(detalle.getCantidad())
                .stockAnterior(detalle.getStockAnterior())
                .stockNuevo(detalle.getStockNuevo())
                .costoUnitario(detalle.getCostoUnitario())
                .costoTotal(detalle.getCostoTotal())
                .observaciones(detalle.getObservaciones())
                .fechaRegistro(detalle.getFechaRegistro())
                .fechaActualizacion(detalle.getFechaActualizacion())
                .build();
    }
}
