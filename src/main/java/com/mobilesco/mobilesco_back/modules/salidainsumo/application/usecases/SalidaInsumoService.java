package com.mobilesco.mobilesco_back.modules.salidainsumo.application.usecases;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mobilesco.mobilesco_back.dto.common.PageResponseDTO;
import com.mobilesco.mobilesco_back.modules.salidainsumo.infrastructure.in.api.dtos.DetalleSalidaInsumoCreateDTO;
import com.mobilesco.mobilesco_back.modules.salidainsumo.infrastructure.in.api.dtos.DetalleSalidaInsumoResponseDTO;
import com.mobilesco.mobilesco_back.modules.salidainsumo.infrastructure.in.api.dtos.SalidaInsumoCreateDTO;
import com.mobilesco.mobilesco_back.modules.salidainsumo.infrastructure.in.api.dtos.SalidaInsumoResponseDTO;
import com.mobilesco.mobilesco_back.modules.shared.application.exceptions.ResourceNotFoundException;
import com.mobilesco.mobilesco_back.modules.shared.application.exceptions.ValidationException;
import com.mobilesco.mobilesco_back.modules.salidainsumo.domain.models.DetalleSalidaInsumoModel;
import com.mobilesco.mobilesco_back.modules.insumo.domain.models.InsumoModel;
import com.mobilesco.mobilesco_back.modules.insumo.application.usecases.StockMinimoNotificacionService;
import com.mobilesco.mobilesco_back.modules.kardex.application.usecases.KardexService;
import com.mobilesco.mobilesco_back.modules.salidainsumo.domain.models.SalidaInsumoModel;
import com.mobilesco.mobilesco_back.modules.salidainsumo.infrastructure.out.persistence.repositories.DetalleSalidaInsumoRepository;
import com.mobilesco.mobilesco_back.modules.insumo.infrastructure.out.persistence.repositories.InsumoRepository;
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
    private final KardexService kardexService;
    private final UsuarioRepository usuarioRepository;
    private final StockMinimoNotificacionService stockMinimoNotificacionService;

    @Transactional
    public SalidaInsumoResponseDTO crear(SalidaInsumoCreateDTO dto) {
        String tipoSalida = normalizarTipoSalida(dto.getTipoSalida());
        String ordenProduccion = normalizarTexto(dto.getOrdenProduccion());

        if ("DIRECTA".equals(tipoSalida) && ordenProduccion == null) {
            throw new ValidationException("La orden de producción es obligatoria para salidas directas");
        }

        if ("INDIRECTA".equals(tipoSalida)) {
            ordenProduccion = null;
        }

        log.info("Creando salida de insumos tipo: {}, orden: {}", tipoSalida, ordenProduccion);

        if (dto.getDetalles() == null || dto.getDetalles().isEmpty()) {
            throw new ValidationException("La salida debe tener al menos un insumo");
        }

        SalidaInsumoModel salida = SalidaInsumoModel.builder()
                .tipoSalida(tipoSalida)
                .ordenProduccion(ordenProduccion)
                .fechaSalida(dto.getFechaSalida() != null ? dto.getFechaSalida() : LocalDateTime.now())
                .observaciones(dto.getObservaciones())
                .responsable(dto.getResponsable() != null ? dto.getResponsable().trim() : null)
                .area(normalizarTexto(dto.getArea()))
                .cantidadTotal(0.0)
                .activo(true)
                .usuario(obtenerUsuarioActual())
                .detalles(new ArrayList<>())
                .build();

        salida = salidaInsumoRepository.save(salida);

        double cantidadTotal = 0.0;
        for (DetalleSalidaInsumoCreateDTO detalleDTO : dto.getDetalles()) {
            DetalleSalidaInsumoModel detalle = procesarDetalle(salida, detalleDTO);
            salida.getDetalles().add(detalle);
            cantidadTotal += detalle.getCantidad();
        }

        salida.setCantidadTotal(cantidadTotal);
        salida = salidaInsumoRepository.save(salida);

        return mapToResponseDTO(salida);
    }

    @Transactional(readOnly = true)
    public List<SalidaInsumoResponseDTO> listar() {
        return salidaInsumoRepository.findByActivoTrueOrderByFechaSalidaDesc()
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PageResponseDTO<SalidaInsumoResponseDTO> listarPaginado(
            String busqueda,
            String area,
            String responsable,
            LocalDateTime fechaInicio,
            LocalDateTime fechaFin,
            Pageable pageable) {
        Page<SalidaInsumoResponseDTO> page = salidaInsumoRepository
                .buscarPaginado(
                        normalizarTexto(busqueda),
                        normalizarTexto(area),
                        normalizarTexto(responsable),
                        fechaInicio,
                        fechaFin,
                        pageable)
                .map(this::mapToResponseDTO);

        return new PageResponseDTO<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages());
    }

    @Transactional(readOnly = true)
    public SalidaInsumoResponseDTO obtenerPorId(Long id) {
        SalidaInsumoModel salida = salidaInsumoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Salida de insumos no encontrada con id: " + id));
        return mapToResponseDTO(salida);
    }

    @Transactional
    public void eliminar(Long id) {
        SalidaInsumoModel salida = salidaInsumoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Salida de insumos no encontrada con id: " + id));

        if (Boolean.FALSE.equals(salida.getActivo())) {
            throw new ValidationException("La salida ya fue eliminada");
        }

        List<DetalleSalidaInsumoModel> detalles = detalleSalidaInsumoRepository.findBySalidaInsumoIdOrderByIdAsc(id);
        if (detalles.isEmpty()) {
            throw new ValidationException("La salida no tiene detalles para revertir");
        }

        String usuario = obtenerUsuarioActual();

        for (DetalleSalidaInsumoModel detalle : detalles) {
            InsumoModel insumo = insumoRepository.findByIdForUpdate(detalle.getInsumo().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Insumo no encontrado con id: " + detalle.getInsumo().getId()));

            Double cantidad = detalle.getCantidad() != null ? detalle.getCantidad() : 0.0;
            Double stockAnterior = insumo.getStockActual() != null ? insumo.getStockActual() : 0.0;
            Double stockNuevo = stockAnterior + cantidad;

            insumo.setStockActual(stockNuevo);
            insumoRepository.save(insumo);

            kardexService.registrarReversaSalida(
                    insumo.getId(),
                    cantidad,
                    detalle.getCostoUnitario(),
                    salida.getId(),
                    "Reversa por eliminacion de salida por error de captura",
                    stockAnterior,
                    stockNuevo,
                    usuario);
        }

        salida.setActivo(false);
        salida.setObservaciones(agregarNotaEliminacion(salida.getObservaciones(), usuario));
        salidaInsumoRepository.save(salida);

        log.info("Salida de insumos eliminada logicamente y stock revertido. ID: {}", id);
    }

    private DetalleSalidaInsumoModel procesarDetalle(SalidaInsumoModel salida, DetalleSalidaInsumoCreateDTO dto) {
        InsumoModel insumo = insumoRepository.findByIdForUpdate(dto.getInsumoId())
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
        stockMinimoNotificacionService.notificarSiCruzaMinimo(insumo, stockAnterior, stockNuevo);

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

        kardexService.registrarSalidaProduccion(
                insumo.getId(),
                cantidad,
                costoUnitario,
                salida.getId(),
                dto.getObservaciones(),
                stockAnterior,
                stockNuevo
        );

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
                .tipoSalida(salida.getTipoSalida())
                .ordenProduccion(salida.getOrdenProduccion())
                .fechaSalida(salida.getFechaSalida())
                .observaciones(salida.getObservaciones())
                .responsable(salida.getResponsable())
                .area(salida.getArea())
                .cantidadTotal(salida.getCantidadTotal())
                .activo(salida.getActivo())
                .usuario(salida.getUsuario())
                .fechaRegistro(salida.getFechaRegistro())
                .fechaActualizacion(salida.getFechaActualizacion())
                .detalles(detalles)
                .build();
    }

    private String normalizarTexto(String valor) {
        if (valor == null) {
            return null;
        }
        String limpio = valor.trim();
        return limpio.isEmpty() ? null : limpio;
    }

    private String normalizarTipoSalida(String valor) {
        String tipo = normalizarTexto(valor);
        if (tipo == null) {
            return "DIRECTA";
        }

        tipo = tipo.toUpperCase(Locale.ROOT);
        if (!"DIRECTA".equals(tipo) && !"INDIRECTA".equals(tipo)) {
            throw new ValidationException("El tipo de salida debe ser DIRECTA o INDIRECTA");
        }

        return tipo;
    }

    private String agregarNotaEliminacion(String observaciones, String usuario) {
        String nota = "Eliminada por error de captura";
        if (usuario != null && !usuario.isBlank()) {
            nota += " por " + usuario;
        }

        String base = normalizarTexto(observaciones);
        String resultado = base == null ? nota : base + " | " + nota;
        return resultado.length() > 500 ? resultado.substring(0, 500) : resultado;
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
