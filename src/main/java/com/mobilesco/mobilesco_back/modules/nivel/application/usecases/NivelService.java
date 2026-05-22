
// RUTA: src/main/java/com/mobilesco/mobilesco_back/services/NivelService.java
package com.mobilesco.mobilesco_back.modules.nivel.application.usecases;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mobilesco.mobilesco_back.modules.nivel.infrastructure.in.api.dtos.NivelCreateDTO;
import com.mobilesco.mobilesco_back.modules.nivel.infrastructure.in.api.dtos.NivelResponseDTO;
import com.mobilesco.mobilesco_back.modules.nivel.infrastructure.in.api.dtos.NivelUpdateDTO;
import com.mobilesco.mobilesco_back.modules.shared.application.exceptions.BadRequestException;
import com.mobilesco.mobilesco_back.modules.shared.application.exceptions.NotFoundException;
import com.mobilesco.mobilesco_back.modules.shared.infrastructure.excel.ExcelReportBuilder;
import com.mobilesco.mobilesco_back.modules.nivel.domain.models.NivelModel;
import com.mobilesco.mobilesco_back.modules.nivel.infrastructure.out.persistence.repositories.NivelRepository;
import com.mobilesco.mobilesco_back.modules.producto.infrastructure.out.persistence.repositories.ProductoRepository;

@Service
public class NivelService {

    private final NivelRepository nivelRepository;
    private final ProductoRepository productoRepository;

    public NivelService(NivelRepository nivelRepository, ProductoRepository productoRepository) {
        this.nivelRepository = nivelRepository;
        this.productoRepository = productoRepository;
    }

    // ========== MAPPER ==========
    
    private NivelResponseDTO mapToResponseDTO(NivelModel nivel) {
        NivelResponseDTO dto = new NivelResponseDTO();
        dto.setId(nivel.getId());
        dto.setCodigo(nivel.getCodigo());
        dto.setNombre(nivel.getNombre());
        dto.setDescripcion(nivel.getDescripcion());
        dto.setActivo(nivel.getActivo());
        dto.setCreatedAt(nivel.getCreatedAt());
        return dto;
    }
    
    private List<NivelResponseDTO> mapToResponseDTOList(List<NivelModel> niveles) {
        return niveles.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }
    
    // ========== CREATE ==========
    
    public NivelResponseDTO crear(NivelCreateDTO dto) {
        
        if (nivelRepository.existsByCodigo(dto.getCodigo())) {
            throw new BadRequestException("Ya existe un nivel con el código: " + dto.getCodigo());
        }
        
        if (nivelRepository.existsByNombre(dto.getNombre())) {
            throw new BadRequestException("Ya existe un nivel con el nombre: " + dto.getNombre());
        }
        
        NivelModel nivel = new NivelModel();
        nivel.setCodigo(dto.getCodigo());
        nivel.setNombre(dto.getNombre());
        nivel.setDescripcion(dto.getDescripcion());
        nivel.setActivo(true);
        
        NivelModel guardado = nivelRepository.save(nivel);
        return mapToResponseDTO(guardado);
    }
    
    // ========== READ ==========
    
    public List<NivelResponseDTO> obtenerTodos() {
        return mapToResponseDTOList(nivelRepository.findAll());
    }

    @Transactional(readOnly = true)
    public byte[] generarReporteExcel(Boolean activo, String busqueda, String sortBy, String direction) {
        List<NivelResponseDTO> niveles = mapToResponseDTOList(nivelRepository.findAll());

        List<NivelResponseDTO> filtrados = niveles.stream()
                .filter(nivel -> activo == null || Objects.equals(nivel.getActivo(), activo))
                .filter(nivel -> coincideBusqueda(nivel, busqueda))
                .collect(Collectors.toList());

        String[] headers = {
                "ID", "Codigo", "Nombre", "Descripcion", "Estado", "Creado"
        };

        return ExcelReportBuilder.generate(
                "Niveles",
                "Reporte de niveles",
                headers,
                filtrados.stream()
                        .map(nivel -> new Object[] {
                                nivel.getId() != null ? nivel.getId() : 0L,
                                nivel.getCodigo() == null ? "" : nivel.getCodigo(),
                                nivel.getNombre() == null ? "" : nivel.getNombre(),
                                nivel.getDescripcion() == null ? "" : nivel.getDescripcion(),
                                Boolean.TRUE.equals(nivel.getActivo()) ? "Activo" : "Inactivo",
                                nivel.getCreatedAt() != null ? nivel.getCreatedAt().toString() : ""
                        })
                        .collect(Collectors.toList()));
    }
    
    public List<NivelResponseDTO> obtenerActivos() {
        return mapToResponseDTOList(nivelRepository.findByActivo(true));
    }
    
    public NivelResponseDTO obtenerPorId(Long id) {
        NivelModel nivel = nivelRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Nivel no encontrado con ID: " + id));
        return mapToResponseDTO(nivel);
    }
    
    public NivelResponseDTO obtenerPorCodigo(String codigo) {
        NivelModel nivel = nivelRepository.findByCodigo(codigo)
                .orElseThrow(() -> new NotFoundException("Nivel no encontrado con código: " + codigo));
        return mapToResponseDTO(nivel);
    }
    
    public NivelResponseDTO obtenerPorNombre(String nombre) {
        NivelModel nivel = nivelRepository.findByNombre(nombre)
                .orElseThrow(() -> new NotFoundException("Nivel no encontrado con nombre: " + nombre));
        return mapToResponseDTO(nivel);
    }

    @Transactional
    public NivelResponseDTO activar(Long id) {
        NivelModel nivel = nivelRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Nivel no encontrado con ID: " + id));
        nivel.setActivo(true);
        return mapToResponseDTO(nivelRepository.save(nivel));
    }

    @Transactional
    public NivelResponseDTO desactivar(Long id) {
        NivelModel nivel = nivelRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Nivel no encontrado con ID: " + id));
        nivel.setActivo(false);
        return mapToResponseDTO(nivelRepository.save(nivel));
    }
    
    // ========== UPDATE ==========
    
    public NivelResponseDTO actualizar(Long id, NivelUpdateDTO dto) {
        
        NivelModel existente = nivelRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Nivel no encontrado con ID: " + id));
        
        // Validar código único
        if (dto.getCodigo() != null && !dto.getCodigo().equals(existente.getCodigo())) {
            if (nivelRepository.existsByCodigo(dto.getCodigo())) {
                throw new BadRequestException("Ya existe un nivel con el código: " + dto.getCodigo());
            }
            existente.setCodigo(dto.getCodigo());
        }
        
        // Validar nombre único
        if (dto.getNombre() != null && !dto.getNombre().equals(existente.getNombre())) {
            if (nivelRepository.existsByNombre(dto.getNombre())) {
                throw new BadRequestException("Ya existe un nivel con el nombre: " + dto.getNombre());
            }
            existente.setNombre(dto.getNombre());
        }
        
        if (dto.getDescripcion() != null) {
            existente.setDescripcion(dto.getDescripcion());
        }
        
        if (dto.getActivo() != null) {
            existente.setActivo(dto.getActivo());
        }
        
        NivelModel actualizado = nivelRepository.save(existente);
        return mapToResponseDTO(actualizado);
    }
    
    // ========== DELETE ==========
    
    public void eliminar(Long id) {
        if (!nivelRepository.existsById(id)) {
            throw new NotFoundException("Nivel no encontrado con ID: " + id);
        }

        if (productoRepository.existsByNivelId(id)) {
            throw new BadRequestException("No se puede eliminar el nivel porque tiene productos asociados");
        }

        nivelRepository.deleteById(id);
    }

    private boolean coincideBusqueda(NivelResponseDTO nivel, String busqueda) {
        if (busqueda == null || busqueda.isBlank()) {
            return true;
        }

        String termino = busqueda.trim().toLowerCase(Locale.ROOT);
        return Stream.of(
                        nivel.getId() != null ? String.valueOf(nivel.getId()) : null,
                        nivel.getCodigo(),
                        nivel.getNombre(),
                        nivel.getDescripcion(),
                        nivel.getActivo() != null ? (nivel.getActivo() ? "activo" : "inactivo") : null,
                        nivel.getCreatedAt() != null ? nivel.getCreatedAt().toString() : null)
                .filter(valor -> valor != null && !valor.isBlank())
                .map(valor -> valor.toLowerCase(Locale.ROOT))
                .anyMatch(valor -> valor.contains(termino));
    }
}
