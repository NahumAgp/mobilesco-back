package com.mobilesco.mobilesco_back.modules.insumo.application.usecases;

import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import com.mobilesco.mobilesco_back.modules.insumo.domain.models.*;
import com.mobilesco.mobilesco_back.modules.insumo.infrastructure.in.api.dtos.ConjuntoInsumoDTO;
import com.mobilesco.mobilesco_back.modules.insumo.infrastructure.out.persistence.repositories.InsumoRepository;
import com.mobilesco.mobilesco_back.modules.unidadmedida.infrastructure.out.persistence.repositories.UnidadMedidaRepository;
import com.mobilesco.mobilesco_back.modules.shared.application.exceptions.ResourceNotFoundException;
import com.mobilesco.mobilesco_back.modules.shared.application.exceptions.ValidationException;

@Service
@RequiredArgsConstructor
public class ConjuntoInsumoService {
    private final InsumoRepository insumoRepository;
    private final UnidadMedidaRepository unidadMedidaRepository;

    @Transactional(readOnly = true)
    public List<ConjuntoInsumoDTO> listar() {
        return insumoRepository.findByConjuntoTrueOrderByNombreAsc().stream().map(ConjuntoInsumoService::mapear).toList();
    }

    @Transactional
    public ConjuntoInsumoDTO guardar(Long id, ConjuntoInsumoDTO dto) {
        InsumoModel conjunto = id == null ? InsumoModel.builder().conjunto(true)
                .codigo("CON-" + UUID.randomUUID()).build()
                : insumoRepository.findByIdForUpdate(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Conjunto no encontrado"));
        if (!conjunto.isConjunto()) throw new ValidationException("El registro no es un conjunto");
        if (id != null && !Objects.equals(dto.version(), conjunto.getVersion())) {
            throw new ValidationException("El conjunto cambio durante la edicion. Vuelve a abrirlo para actualizarlo.");
        }
        String nombre = dto.nombre() == null ? "" : dto.nombre().trim();
        if (nombre.isEmpty()) throw new ValidationException("Captura el nombre del conjunto");
        if (!nombre.equalsIgnoreCase(conjunto.getNombre()) && insumoRepository.existsByNombreIgnoreCase(nombre)) {
            throw new ValidationException("Ya existe un insumo o conjunto con ese nombre");
        }
        var unidad = unidadMedidaRepository.findById(dto.unidadMedidaId())
                .orElseThrow(() -> new ResourceNotFoundException("Unidad no encontrada"));
        if (!Boolean.TRUE.equals(unidad.getEstado())) throw new ValidationException("La unidad esta inactiva");
        if (dto.componentes() == null || dto.componentes().isEmpty()) throw new ValidationException("Agrega al menos un insumo");
        Map<Long, ComponenteInsumoModel> existentes = new HashMap<>();
        conjunto.getComponentes().forEach(c -> existentes.put(c.getInsumo().getId(), c));
        Set<Long> ids = new HashSet<>();
        for (var item : dto.componentes()) {
            if (item == null || item.insumoId() == null || !ids.add(item.insumoId())) {
                throw new ValidationException("No repitas insumos dentro del mismo conjunto; ajusta su cantidad");
            }
            if (item.cantidad() == null || !Double.isFinite(item.cantidad()) || item.cantidad() <= 0) {
                throw new ValidationException("La cantidad de cada componente debe ser mayor a cero");
            }
            var insumo = insumoRepository.findById(item.insumoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Insumo no encontrado"));
            if (insumo.isConjunto()) throw new ValidationException("El despiece debe contener insumos directos");
            if (!Boolean.TRUE.equals(insumo.getActivo()) && !existentes.containsKey(insumo.getId())) {
                throw new ValidationException("El insumo " + insumo.getNombre() + " esta inactivo");
            }
            var componente = existentes.get(insumo.getId());
            if (componente == null) {
                componente = ComponenteInsumoModel.builder().conjunto(conjunto).insumo(insumo).build();
                conjunto.getComponentes().add(componente);
            }
            componente.setCantidad(item.cantidad());
        }
        conjunto.getComponentes().removeIf(c -> !ids.contains(c.getInsumo().getId()));
        conjunto.setNombre(nombre);
        conjunto.setDescripcion(dto.descripcion());
        conjunto.setUnidadMedida(unidad);
        // Fuerza una nueva version tambien cuando solo cambia el despiece.
        conjunto.setFechaActualizacion(java.time.LocalDateTime.now());
        return mapear(insumoRepository.saveAndFlush(conjunto));
    }

    public static ConjuntoInsumoDTO mapear(InsumoModel conjunto) {
        return new ConjuntoInsumoDTO(conjunto.getId(), conjunto.getNombre(), conjunto.getDescripcion(),
                conjunto.getUnidadMedida().getId(), conjunto.getUnidadMedida().getSimbolo(),
                conjunto.getCostoCotizacion(), conjunto.getVersion(), componentes(conjunto));
    }

    public static List<ConjuntoInsumoDTO.Componente> componentes(InsumoModel insumo) {
        if (!insumo.isConjunto()) return List.of();
        return insumo.getComponentes().stream().map(c -> new ConjuntoInsumoDTO.Componente(
                c.getInsumo().getId(), c.getInsumo().getNombre(), c.getInsumo().getUnidadMedida().getSimbolo(),
                c.getCantidad(), c.getInsumo().getCostoCotizacion())).toList();
    }
}
