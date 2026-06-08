package com.mobilesco.mobilesco_back.modules.tipoinsumo.application.usecases;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.mobilesco.mobilesco_back.modules.shared.application.exceptions.BadRequestException;
import com.mobilesco.mobilesco_back.modules.shared.application.exceptions.NotFoundException;
import com.mobilesco.mobilesco_back.modules.tipoinsumo.domain.models.TipoInsumoModel;
import com.mobilesco.mobilesco_back.modules.tipoinsumo.infrastructure.in.api.dtos.TipoInsumoCodigoPreviewDTO;
import com.mobilesco.mobilesco_back.modules.tipoinsumo.infrastructure.in.api.dtos.TipoInsumoCreateDTO;
import com.mobilesco.mobilesco_back.modules.tipoinsumo.infrastructure.in.api.dtos.TipoInsumoResponseDTO;
import com.mobilesco.mobilesco_back.modules.tipoinsumo.infrastructure.in.api.dtos.TipoInsumoUpdateDTO;
import com.mobilesco.mobilesco_back.modules.tipoinsumo.infrastructure.out.persistence.repositories.TipoInsumoRepository;

@Service
public class TipoInsumoService {

    private static final int CODIGO_MAXIMO_CARACTERES = 3;

    private final TipoInsumoRepository tipoInsumoRepository;

    public TipoInsumoService(TipoInsumoRepository tipoInsumoRepository) {
        this.tipoInsumoRepository = tipoInsumoRepository;
    }

    public List<TipoInsumoResponseDTO> listar(boolean soloActivos) {
        List<TipoInsumoModel> tipos = soloActivos
                ? tipoInsumoRepository.findByActivoTrueOrderByNombreAsc()
                : tipoInsumoRepository.findAllByOrderByActivoDescNombreAsc();

        return tipos.stream().map(this::mapToResponseDTO).toList();
    }

    public TipoInsumoCodigoPreviewDTO previsualizarCodigo(String nombre) {
        TipoInsumoCodigoPreviewDTO dto = new TipoInsumoCodigoPreviewDTO();
        String nombreLimpio = limpiarTexto(nombre);

        if (!StringUtils.hasText(nombreLimpio)) {
            dto.setCodigo("");
            dto.setDisponible(false);
            dto.setMensaje("Escribe un nombre para generar el codigo");
            return dto;
        }

        Optional<TipoInsumoModel> existente = tipoInsumoRepository
                .findByNombreNormalizado(TipoInsumoModel.normalizarNombre(nombreLimpio));

        if (existente.isPresent()) {
            dto.setCodigo(existente.get().getCodigo());
            dto.setDisponible(false);
            dto.setMensaje("Ya existe un tipo de insumo con ese nombre");
            return dto;
        }

        Optional<String> codigoDisponible = generarCodigoDisponible(nombreLimpio);

        if (codigoDisponible.isPresent()) {
            dto.setCodigo(codigoDisponible.get());
            dto.setDisponible(true);
            dto.setMensaje("Codigo disponible");
            return dto;
        }

        dto.setCodigo(generarUltimoCodigoIntentado(nombreLimpio));
        dto.setDisponible(false);
        dto.setMensaje("No fue posible generar un codigo unico de hasta 3 caracteres");
        return dto;
    }

    public TipoInsumoResponseDTO crear(TipoInsumoCreateDTO dto) {
        String nombre = limpiarNombreObligatorio(dto.getNombre());
        validarNombreUnico(nombre, null);

        TipoInsumoModel tipo = new TipoInsumoModel();
        tipo.setNombre(nombre);
        tipo.setDescripcion(limpiarTexto(dto.getDescripcion()));
        tipo.setCodigo(generarCodigoObligatorio(nombre));
        tipo.setActivo(true);

        return mapToResponseDTO(tipoInsumoRepository.save(tipo));
    }

    public TipoInsumoResponseDTO actualizar(Long id, TipoInsumoUpdateDTO dto) {
        TipoInsumoModel tipo = tipoInsumoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Tipo de insumo no encontrado"));

        String nombre = limpiarNombreObligatorio(dto.getNombre());
        validarNombreUnico(nombre, id);

        tipo.setNombre(nombre);
        tipo.setDescripcion(limpiarTexto(dto.getDescripcion()));

        return mapToResponseDTO(tipoInsumoRepository.save(tipo));
    }

    public TipoInsumoResponseDTO actualizarEstado(Long id, Boolean activo) {
        if (activo == null) {
            throw new BadRequestException("Debes indicar si el tipo de insumo estara activo");
        }

        TipoInsumoModel tipo = tipoInsumoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Tipo de insumo no encontrado"));

        tipo.setActivo(activo);
        return mapToResponseDTO(tipoInsumoRepository.save(tipo));
    }

    private TipoInsumoResponseDTO mapToResponseDTO(TipoInsumoModel model) {
        TipoInsumoResponseDTO dto = new TipoInsumoResponseDTO();
        dto.setId(model.getId());
        dto.setCodigo(model.getCodigo());
        dto.setNombre(model.getNombre());
        dto.setDescripcion(model.getDescripcion());
        dto.setActivo(model.getActivo());
        dto.setFechaRegistro(model.getFechaRegistro());
        return dto;
    }

    private void validarNombreUnico(String nombre, Long idActual) {
        tipoInsumoRepository.findByNombreNormalizado(TipoInsumoModel.normalizarNombre(nombre))
                .ifPresent(existente -> {
                    if (idActual == null || !existente.getId().equals(idActual)) {
                        throw new BadRequestException("Ya existe un tipo de insumo con ese nombre");
                    }
                });
    }

    private String limpiarNombreObligatorio(String nombre) {
        String limpio = limpiarTexto(nombre);
        if (!StringUtils.hasText(limpio)) {
            throw new BadRequestException("El nombre del tipo de insumo es obligatorio");
        }
        return limpio;
    }

    private String limpiarTexto(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }

        return value.trim().replaceAll("\\s+", " ");
    }

    private String generarCodigoObligatorio(String nombre) {
        return generarCodigoDisponible(nombre)
                .orElseThrow(() -> new BadRequestException(
                        "No fue posible generar un codigo unico con maximo 3 caracteres para ese nombre"));
    }

    private Optional<String> generarCodigoDisponible(String nombre) {
        String base = generarBaseCodigo(nombre);

        for (int longitud = 1; longitud <= Math.min(CODIGO_MAXIMO_CARACTERES, base.length()); longitud += 1) {
            String candidato = base.substring(0, longitud);
            if (tipoInsumoRepository.findByCodigoIgnoreCase(candidato).isEmpty()) {
                return Optional.of(candidato);
            }
        }

        return Optional.empty();
    }

    private String generarUltimoCodigoIntentado(String nombre) {
        String base = generarBaseCodigo(nombre);
        int longitud = Math.min(CODIGO_MAXIMO_CARACTERES, base.length());
        return base.substring(0, longitud);
    }

    private String generarBaseCodigo(String nombre) {
        String sinAcentos = Normalizer.normalize(nombre, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "");

        String base = sinAcentos
                .toUpperCase(Locale.ROOT)
                .replaceAll("[^A-Z0-9]+", "");

        if (!StringUtils.hasText(base)) {
            return "TIP";
        }

        if (base.length() <= CODIGO_MAXIMO_CARACTERES) {
            return base;
        }

        return base.substring(0, CODIGO_MAXIMO_CARACTERES);
    }
}
