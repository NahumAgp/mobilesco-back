package com.mobilesco.mobilesco_back.modules.areatrabajo.application.usecases;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.mobilesco.mobilesco_back.dto.common.PageResponseDTO;
import com.mobilesco.mobilesco_back.modules.areatrabajo.domain.models.AreaTrabajoModel;
import com.mobilesco.mobilesco_back.modules.areatrabajo.infrastructure.in.api.dtos.AreaTrabajoCreateDTO;
import com.mobilesco.mobilesco_back.modules.areatrabajo.infrastructure.in.api.dtos.AreaTrabajoResponseDTO;
import com.mobilesco.mobilesco_back.modules.areatrabajo.infrastructure.in.api.dtos.AreaTrabajoUpdateDTO;
import com.mobilesco.mobilesco_back.modules.areatrabajo.infrastructure.out.persistence.repositories.AreaTrabajoRepository;
import com.mobilesco.mobilesco_back.modules.shared.application.exceptions.BadRequestException;
import com.mobilesco.mobilesco_back.modules.shared.application.exceptions.NotFoundException;

import jakarta.transaction.Transactional;

@Service
public class AreaTrabajoService {

    private static final int MAX_CODE_LENGTH = 10;
    private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    private final AreaTrabajoRepository areaTrabajoRepository;

    public AreaTrabajoService(AreaTrabajoRepository areaTrabajoRepository) {
        this.areaTrabajoRepository = areaTrabajoRepository;
    }

    @Transactional
    public AreaTrabajoResponseDTO crear(AreaTrabajoCreateDTO dto) {
        String nombre = normalizarTexto(dto.getNombre());
        String codigo = sugerirCodigo(nombre);

        if (areaTrabajoRepository.existsByCodigoIgnoreCase(codigo)) {
            throw new BadRequestException("Ya existe un area con ese codigo.");
        }

        if (areaTrabajoRepository.existsByNombreIgnoreCase(nombre)) {
            throw new BadRequestException("Ya existe un area con ese nombre.");
        }

        AreaTrabajoModel area = new AreaTrabajoModel();
        area.setCodigo(codigo);
        area.setNombre(nombre);
        area.setDescripcion(normalizarTexto(dto.getDescripcion()));
        area.setActivo(true);

        return map(areaTrabajoRepository.save(area));
    }

    public String sugerirCodigo(String nombre) {
        return generarCodigoDesdeNombre(nombre, areaTrabajoRepository.findAll().stream()
                .map(AreaTrabajoModel::getCodigo)
                .toList());
    }

    @Transactional
    public AreaTrabajoResponseDTO actualizar(Long id, AreaTrabajoUpdateDTO dto) {
        AreaTrabajoModel area = areaTrabajoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Area de trabajo no encontrada"));

        String codigo = normalizarCodigo(dto.getCodigo());
        String nombre = normalizarTexto(dto.getNombre());

        areaTrabajoRepository.findByCodigoIgnoreCase(codigo)
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> { throw new BadRequestException("Ya existe un area con ese codigo."); });

        if (!area.getNombre().equalsIgnoreCase(nombre) && areaTrabajoRepository.existsByNombreIgnoreCase(nombre)) {
            throw new BadRequestException("Ya existe un area con ese nombre.");
        }

        area.setCodigo(codigo);
        area.setNombre(nombre);
        area.setDescripcion(normalizarTexto(dto.getDescripcion()));
        if (dto.getActivo() != null) {
            area.setActivo(dto.getActivo());
        }

        return map(areaTrabajoRepository.save(area));
    }

    public AreaTrabajoResponseDTO obtenerPorId(Long id) {
        return areaTrabajoRepository.findById(id)
                .map(this::map)
                .orElseThrow(() -> new NotFoundException("Area de trabajo no encontrada"));
    }

    public List<AreaTrabajoResponseDTO> listar(Boolean soloActivas) {
        List<AreaTrabajoModel> areas = Boolean.TRUE.equals(soloActivas)
                ? areaTrabajoRepository.findByActivoTrueOrderByNombreAsc()
                : areaTrabajoRepository.findAllByOrderByNombreAsc();

        return areas.stream().map(this::map).toList();
    }

    public PageResponseDTO<AreaTrabajoResponseDTO> listarPaginado(Boolean activo, String busqueda, Pageable pageable) {
        Page<AreaTrabajoResponseDTO> page = areaTrabajoRepository
                .buscarPaginado(activo, normalizarFiltro(busqueda), pageable)
                .map(this::map);

        return new PageResponseDTO<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages());
    }

    @Transactional
    public AreaTrabajoResponseDTO cambiarActivo(Long id, Boolean activo) {
        AreaTrabajoModel area = areaTrabajoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Area de trabajo no encontrada"));
        area.setActivo(activo);
        return map(areaTrabajoRepository.save(area));
    }

    private AreaTrabajoResponseDTO map(AreaTrabajoModel area) {
        AreaTrabajoResponseDTO dto = new AreaTrabajoResponseDTO();
        dto.setId(area.getId());
        dto.setCodigo(area.getCodigo());
        dto.setNombre(area.getNombre());
        dto.setDescripcion(area.getDescripcion());
        dto.setActivo(area.getActivo());
        dto.setFechaRegistro(area.getFechaRegistro());
        dto.setFechaActualizacion(area.getFechaActualizacion());
        return dto;
    }

    private String normalizarCodigo(String value) {
        return value == null ? "" : value.trim().toUpperCase(Locale.ROOT).replaceAll("\\s+", "_");
    }

    private String normalizarTexto(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ");
    }

    private String normalizarFiltro(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim().replaceAll("\\s+", " ");
    }

    private String generarCodigoDesdeNombre(String nombre, Collection<String> codigosExistentes) {
        List<String> palabras = normalizarPalabrasCodigo(nombre);
        Set<String> codigosUsados = normalizarCodigosExistentes(codigosExistentes);

        List<String> candidatos = construirCandidatosCodigo(palabras);
        for (String candidato : candidatos) {
            if (!codigosUsados.contains(candidato)) {
                return candidato;
            }
        }

        String semilla = candidatos.get(candidatos.size() - 1);
        String fallback = primerFallbackAlfabetico(semilla, codigosUsados);
        if (fallback != null) {
            return fallback;
        }

        throw new BadRequestException("No hay codigos alfabeticos disponibles para el area");
    }

    private List<String> normalizarPalabrasCodigo(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new BadRequestException("El nombre es obligatorio para generar el codigo del area");
        }

        String normalizado = Normalizer.normalize(nombre, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toUpperCase(Locale.ROOT)
                .replaceAll("[^A-Z]+", " ")
                .trim();

        if (normalizado.isBlank()) {
            throw new BadRequestException("El nombre del area debe contener letras para generar el codigo");
        }

        return List.of(normalizado.split("\\s+"));
    }

    private Set<String> normalizarCodigosExistentes(Collection<String> codigosExistentes) {
        if (codigosExistentes == null) {
            return Set.of();
        }

        return codigosExistentes.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(code -> !code.isBlank())
                .map(code -> code.toUpperCase(Locale.ROOT))
                .collect(Collectors.toCollection(HashSet::new));
    }

    private List<String> construirCandidatosCodigo(List<String> palabras) {
        Set<String> candidatos = new LinkedHashSet<>();
        String base = completarCodigo(palabras.get(0), 2);

        if (palabras.size() == 1) {
            agregarCandidatosExpandidos(candidatos, base, letrasRestantes(palabras.get(0), 2));
            return new ArrayList<>(candidatos);
        }

        StringBuilder iniciales = new StringBuilder(base);
        StringBuilder letrasExtra = new StringBuilder();

        for (int i = 1; i < palabras.size(); i++) {
            String palabra = palabras.get(i);
            iniciales.append(palabra.charAt(0));
            letrasExtra.append(letrasRestantes(palabra, 1));
        }

        agregarCandidatosExpandidos(candidatos, iniciales.toString(), letrasExtra.toString());
        return new ArrayList<>(candidatos);
    }

    private void agregarCandidatosExpandidos(Set<String> candidatos, String inicial, String letrasExtra) {
        String actual = truncarCodigo(inicial);
        candidatos.add(actual);

        for (int i = 0; i < letrasExtra.length() && actual.length() < MAX_CODE_LENGTH; i++) {
            actual = actual + letrasExtra.charAt(i);
            candidatos.add(actual);
        }
    }

    private String letrasRestantes(String palabra, int letrasUsadas) {
        if (palabra.length() <= letrasUsadas) {
            return "";
        }
        return palabra.substring(letrasUsadas);
    }

    private String completarCodigo(String palabra, int longitud) {
        StringBuilder resultado = new StringBuilder(palabra);
        while (resultado.length() < longitud) {
            resultado.append(palabra.charAt(resultado.length() % palabra.length()));
        }
        return resultado.substring(0, longitud);
    }

    private String truncarCodigo(String value) {
        return value.length() <= MAX_CODE_LENGTH ? value : value.substring(0, MAX_CODE_LENGTH);
    }

    private String primerFallbackAlfabetico(String semilla, Set<String> codigosUsados) {
        for (int suffixLength = 1; suffixLength < MAX_CODE_LENGTH; suffixLength++) {
            int prefixLength = Math.min(semilla.length(), MAX_CODE_LENGTH - suffixLength);
            if (prefixLength <= 0) {
                continue;
            }

            String prefix = semilla.substring(0, prefixLength);
            String disponible = primeroConSufijo(prefix, suffixLength, "", codigosUsados);
            if (disponible != null) {
                return disponible;
            }
        }

        return null;
    }

    private String primeroConSufijo(String prefix, int remaining, String suffix, Set<String> codigosUsados) {
        if (remaining == 0) {
            String candidate = prefix + suffix;
            return codigosUsados.contains(candidate) ? null : candidate;
        }

        for (int i = 0; i < ALPHABET.length(); i++) {
            String disponible = primeroConSufijo(prefix, remaining - 1, suffix + ALPHABET.charAt(i), codigosUsados);
            if (disponible != null) {
                return disponible;
            }
        }

        return null;
    }
}
