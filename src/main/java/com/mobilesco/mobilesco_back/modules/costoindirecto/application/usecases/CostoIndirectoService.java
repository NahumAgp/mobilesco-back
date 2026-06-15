package com.mobilesco.mobilesco_back.modules.costoindirecto.application.usecases;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mobilesco.mobilesco_back.modules.costoindirecto.domain.enums.BaseDistribucion;
import com.mobilesco.mobilesco_back.modules.costoindirecto.domain.enums.PeriodicidadCostoIndirecto;
import com.mobilesco.mobilesco_back.modules.costoindirecto.infrastructure.in.api.dtos.CostoIndirectoCreateDTO;
import com.mobilesco.mobilesco_back.modules.costoindirecto.infrastructure.in.api.dtos.CostoIndirectoResponseDTO;
import com.mobilesco.mobilesco_back.modules.costoindirecto.infrastructure.in.api.dtos.CostoIndirectoUpdateDTO;
import com.mobilesco.mobilesco_back.modules.costoindirecto.infrastructure.in.api.dtos.CifConfiguracionDTO;
import com.mobilesco.mobilesco_back.modules.costoindirecto.infrastructure.in.api.dtos.CifResumenDTO;
import com.mobilesco.mobilesco_back.modules.costoindirecto.domain.enums.TipoCostoIndirecto;
import com.mobilesco.mobilesco_back.modules.shared.application.exceptions.ResourceNotFoundException;
import com.mobilesco.mobilesco_back.modules.shared.application.exceptions.ValidationException;
import com.mobilesco.mobilesco_back.modules.costoindirecto.domain.models.CifConfiguracionModel;
import com.mobilesco.mobilesco_back.modules.costoindirecto.domain.models.CostoIndirectoModel;
import com.mobilesco.mobilesco_back.modules.costoindirecto.infrastructure.out.persistence.repositories.CifConfiguracionRepository;
import com.mobilesco.mobilesco_back.modules.costoindirecto.infrastructure.out.persistence.repositories.CostoIndirectoRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CostoIndirectoService {

    private final CostoIndirectoRepository costoIndirectoRepository;
    private final CifConfiguracionRepository cifConfiguracionRepository;

    @Transactional
    public CostoIndirectoResponseDTO crear(CostoIndirectoCreateDTO dto) {
        String codigo = normalizarCodigo(dto.getCodigo());
        log.info("Creando nuevo costo indirecto con codigo: {}", codigo);

        // Validar código único
        if (costoIndirectoRepository.existsByCodigoIgnoreCase(codigo)) {
            throw new ValidationException("Ya existe un costo indirecto con el codigo: " + codigo);
        }

        // Validaciones según tipo
        validarMonto(dto.getMontoMensual());

        // Crear entidad
        CostoIndirectoModel costo = CostoIndirectoModel.builder()
                .codigo(codigo)
                .nombre(dto.getNombre())
                .descripcion(dto.getDescripcion())
                .tipo(dto.getTipo())
                .baseDistribucion(dto.getBaseDistribucion() != null ? dto.getBaseDistribucion() : BaseDistribucion.HORAS_MOD)
                .montoMensual(dto.getMontoMensual())
                .periodicidad(dto.getPeriodicidad() != null ? dto.getPeriodicidad() : PeriodicidadCostoIndirecto.MENSUAL)
                .porcentajeAsignado(dto.getPorcentajeAsignado())
                .tasaVariable(dto.getTasaVariable())
                .activo(true)
                .build();

        CostoIndirectoModel saved = costoIndirectoRepository.save(costo);
        log.info("Costo indirecto creado con ID: {}", saved.getId());

        return mapToResponseDTO(saved);
    }

    @Transactional
    public CostoIndirectoResponseDTO actualizar(Long id, CostoIndirectoUpdateDTO dto) {
        log.info("Actualizando costo indirecto ID: {}", id);

        CostoIndirectoModel costo = costoIndirectoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Costo indirecto no encontrado con id: " + id));

        // Validar código único (excepto si es el mismo)
        String codigo = dto.getCodigo() != null && !dto.getCodigo().isBlank()
                ? normalizarCodigo(dto.getCodigo())
                : costo.getCodigo();

        if (!costo.getCodigo().equalsIgnoreCase(codigo) &&
                costoIndirectoRepository.existsByCodigoIgnoreCase(codigo)) {
            throw new ValidationException("Ya existe un costo indirecto con el codigo: " + codigo);
        }

        // Actualizar campos
        costo.setCodigo(codigo);
        costo.setNombre(dto.getNombre());
        costo.setDescripcion(dto.getDescripcion());
        
        if (dto.getTipo() != null) {
            costo.setTipo(dto.getTipo());
        }
        
        if (dto.getBaseDistribucion() != null) {
            costo.setBaseDistribucion(dto.getBaseDistribucion());
        }
        
        if (dto.getMontoMensual() != null) {
            validarMonto(dto.getMontoMensual());
            costo.setMontoMensual(dto.getMontoMensual());
        }

        if (dto.getPeriodicidad() != null) {
            costo.setPeriodicidad(dto.getPeriodicidad());
        }
        
        if (dto.getPorcentajeAsignado() != null) {
            costo.setPorcentajeAsignado(dto.getPorcentajeAsignado());
        }
        
        if (dto.getTasaVariable() != null) {
            costo.setTasaVariable(dto.getTasaVariable());
        }
        
        if (dto.getActivo() != null) {
            costo.setActivo(dto.getActivo());
        }

        CostoIndirectoModel updated = costoIndirectoRepository.save(costo);
        log.info("Costo indirecto actualizado");

        return mapToResponseDTO(updated);
    }

    @Transactional(readOnly = true)
    public CostoIndirectoResponseDTO obtenerPorId(Long id) {
        CostoIndirectoModel costo = costoIndirectoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Costo indirecto no encontrado con id: " + id));
        return mapToResponseDTO(costo);
    }

    @Transactional(readOnly = true)
    public CostoIndirectoResponseDTO obtenerPorCodigo(String codigo) {
        CostoIndirectoModel costo = costoIndirectoRepository.findByCodigo(codigo)
                .orElseThrow(() -> new ResourceNotFoundException("Costo indirecto no encontrado con código: " + codigo));
        return mapToResponseDTO(costo);
    }

    @Transactional(readOnly = true)
    public List<CostoIndirectoResponseDTO> listar() {
        double minutosProductivosMes = obtenerConfiguracionModel().calcularMinutosProductivosMes();
        return costoIndirectoRepository.findAll()
                .stream()
                .map(costo -> mapToResponseDTO(costo, minutosProductivosMes))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CostoIndirectoResponseDTO> listarActivos() {
        double minutosProductivosMes = obtenerConfiguracionModel().calcularMinutosProductivosMes();
        return costoIndirectoRepository.findByActivoTrue()
                .stream()
                .map(costo -> mapToResponseDTO(costo, minutosProductivosMes))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CostoIndirectoResponseDTO> listarPorTipo(TipoCostoIndirecto tipo) {
        return costoIndirectoRepository.findByTipo(tipo)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CostoIndirectoResponseDTO> buscar(String nombre) {
        return costoIndirectoRepository.buscarPorNombre(nombre)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void eliminar(Long id) {
        log.info("Eliminando costo indirecto ID: {}", id);

        CostoIndirectoModel costo = costoIndirectoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Costo indirecto no encontrado con id: " + id));

        costo.setActivo(false);
        costoIndirectoRepository.save(costo);
        log.info("Costo indirecto desactivado");
    }

    @Transactional
    public CostoIndirectoResponseDTO cambiarEstado(Long id, Boolean activo) {
        CostoIndirectoModel costo = costoIndirectoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Costo indirecto no encontrado con id: " + id));
        costo.setActivo(Boolean.TRUE.equals(activo));
        return mapToResponseDTO(costoIndirectoRepository.save(costo));
    }

    @Transactional
    public CifConfiguracionDTO obtenerConfiguracion() {
        return mapToConfiguracionDTO(obtenerConfiguracionModel());
    }

    @Transactional
    public CifConfiguracionDTO actualizarConfiguracion(CifConfiguracionDTO dto) {
        CifConfiguracionModel config = obtenerConfiguracionModel();
        if (dto.getDiasLaborablesMes() != null) {
            validarPositivo(dto.getDiasLaborablesMes(), "Los dias laborables del mes deben ser mayores a 0");
            config.setDiasLaborablesMes(dto.getDiasLaborablesMes());
        }
        if (dto.getHorasPorDia() != null) {
            validarPositivo(dto.getHorasPorDia(), "Las horas por dia deben ser mayores a 0");
            config.setHorasPorDia(dto.getHorasPorDia());
        }
        if (dto.getTurnos() != null) {
            validarPositivo(dto.getTurnos(), "Los turnos deben ser mayores a 0");
            config.setTurnos(dto.getTurnos());
        }
        if (dto.getNombre() != null && !dto.getNombre().isBlank()) {
            config.setNombre(dto.getNombre().trim());
        }
        return mapToConfiguracionDTO(cifConfiguracionRepository.save(config));
    }

    @Transactional(readOnly = true)
    public CifResumenDTO obtenerResumen() {
        CifConfiguracionModel config = obtenerConfiguracionModel();
        double totalMensual = calcularTotalMensualActivo();
        double minutosProductivosMes = config.calcularMinutosProductivosMes();
        return CifResumenDTO.builder()
                .configuracionId(config.getId())
                .totalMensual(totalMensual)
                .minutosProductivosMes(minutosProductivosMes)
                .costoMinuto(calcularTasaMinuto(totalMensual, minutosProductivosMes))
                .conceptosActivos(costoIndirectoRepository.countByActivoTrue())
                .build();
    }

    @Transactional(readOnly = true)
    public double calcularTasaMinuto() {
        double totalMensual = calcularTotalMensualActivo();
        double minutosProductivosMes = obtenerConfiguracionModel().calcularMinutosProductivosMes();
        return calcularTasaMinuto(totalMensual, minutosProductivosMes);
    }

    @Transactional(readOnly = true)
    public double calcularTotalMensualActivo() {
        return costoIndirectoRepository.findByActivoTrue()
                .stream()
                .mapToDouble(this::calcularMontoMensualEquivalente)
                .sum();
    }

    public double calcularMontoMensualEquivalente(CostoIndirectoModel costo) {
        double monto = nz(costo.getMontoMensual());
        PeriodicidadCostoIndirecto periodicidad = costo.getPeriodicidad() != null
                ? costo.getPeriodicidad()
                : PeriodicidadCostoIndirecto.MENSUAL;

        return switch (periodicidad) {
            case SEMANAL -> monto * 52.0 / 12.0;
            case QUINCENAL -> monto * 2.0;
            case ANUAL -> monto / 12.0;
            case MENSUAL -> monto;
        };
    }

    public CostoIndirectoResponseDTO mapToResponseDTO(CostoIndirectoModel costo, double minutosProductivosMes) {
        double montoMensualEquivalente = calcularMontoMensualEquivalente(costo);
        return CostoIndirectoResponseDTO.builder()
                .id(costo.getId())
                .codigo(costo.getCodigo())
                .nombre(costo.getNombre())
                .descripcion(costo.getDescripcion())
                .tipo(costo.getTipo())
                .baseDistribucion(costo.getBaseDistribucion())
                .montoMensual(costo.getMontoMensual())
                .monto(costo.getMontoMensual())
                .montoMensualEquivalente(montoMensualEquivalente)
                .costoMinuto(calcularTasaMinuto(montoMensualEquivalente, minutosProductivosMes))
                .periodicidad(costo.getPeriodicidad() != null ? costo.getPeriodicidad() : PeriodicidadCostoIndirecto.MENSUAL)
                .porcentajeAsignado(costo.getPorcentajeAsignado())
                .tasaVariable(costo.getTasaVariable())
                .activo(costo.getActivo())
                .fechaRegistro(costo.getFechaRegistro())
                .fechaActualizacion(costo.getFechaActualizacion())
                .build();
    }

    private CostoIndirectoResponseDTO mapToResponseDTO(CostoIndirectoModel costo) {
        return mapToResponseDTO(costo, obtenerConfiguracionModel().calcularMinutosProductivosMes());
    }

    private CifConfiguracionModel obtenerConfiguracionModel() {
        return cifConfiguracionRepository.findFirstByActivoTrueOrderByIdAsc()
                .orElseGet(CifConfiguracionModel::new);
    }

    private CifConfiguracionDTO mapToConfiguracionDTO(CifConfiguracionModel config) {
        return CifConfiguracionDTO.builder()
                .id(config.getId())
                .nombre(config.getNombre())
                .diasLaborablesMes(config.getDiasLaborablesMes())
                .horasPorDia(config.getHorasPorDia())
                .turnos(config.getTurnos())
                .minutosProductivosMes(config.calcularMinutosProductivosMes())
                .activo(config.getActivo())
                .fechaRegistro(config.getFechaRegistro())
                .fechaActualizacion(config.getFechaActualizacion())
                .build();
    }

    private double calcularTasaMinuto(double totalMensual, double minutosProductivosMes) {
        return minutosProductivosMes > 0 ? totalMensual / minutosProductivosMes : 0.0;
    }

    private void validarMonto(Double monto) {
        validarPositivo(monto, "El monto del costo indirecto debe ser mayor a 0");
    }

    private void validarPositivo(Double valor, String mensaje) {
        if (valor == null || valor <= 0) {
            throw new ValidationException(mensaje);
        }
    }

    private double nz(Double value) {
        return value == null ? 0.0 : value;
    }

    private String normalizarCodigo(String codigo) {
        if (codigo != null && !codigo.isBlank()) {
            return codigo.trim().toUpperCase();
        }

        long siguiente = costoIndirectoRepository.count() + 1;
        String generado;
        do {
            generado = String.format("CIF-%05d", siguiente++);
        } while (costoIndirectoRepository.existsByCodigoIgnoreCase(generado));
        return generado;
    }
}
