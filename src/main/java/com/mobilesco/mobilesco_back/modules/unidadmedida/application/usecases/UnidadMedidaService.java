package com.mobilesco.mobilesco_back.modules.unidadmedida.application.usecases;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mobilesco.mobilesco_back.dto.common.PageResponseDTO;
import com.mobilesco.mobilesco_back.modules.unidadmedida.infrastructure.in.api.dtos.UnidadMedidaCreateDTO;
import com.mobilesco.mobilesco_back.modules.unidadmedida.infrastructure.in.api.dtos.UnidadMedidaResponseDTO;
import com.mobilesco.mobilesco_back.modules.unidadmedida.infrastructure.in.api.dtos.UnidadMedidaUpdateDTO;
import com.mobilesco.mobilesco_back.modules.shared.application.exceptions.NotFoundException;
import com.mobilesco.mobilesco_back.modules.shared.infrastructure.excel.ExcelReportBuilder;
import com.mobilesco.mobilesco_back.modules.shared.infrastructure.sort.TypeSafeSorts;
import com.mobilesco.mobilesco_back.modules.unidadmedida.domain.models.UnidadMedidaModel;
import com.mobilesco.mobilesco_back.modules.unidadmedida.infrastructure.out.persistence.repositories.UnidadMedidaRepository;


@Service
public class UnidadMedidaService {

    private static final int PAGE_SIZE = 10;

    private final UnidadMedidaRepository unidadMedidaRepository;

    public UnidadMedidaService(UnidadMedidaRepository unidadMedidaRepository) {
        this.unidadMedidaRepository = unidadMedidaRepository;
    }

    private UnidadMedidaResponseDTO mapToResponseDTO(UnidadMedidaModel unidadMedida) {
        UnidadMedidaResponseDTO dto = new UnidadMedidaResponseDTO();
        dto.setId(unidadMedida.getId());
        dto.setNombre(unidadMedida.getNombre());
        dto.setSimbolo(unidadMedida.getSimbolo());
        dto.setTipo(unidadMedida.getTipo());
        dto.setEstado(unidadMedida.getEstado());
        dto.setFechaRegistro(unidadMedida.getFechaRegistro());
        return dto;
    }

    private List<UnidadMedidaResponseDTO> mapToResponseDTOList(List<UnidadMedidaModel> unidades) {
        return unidades.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    // --------- CREATE ---------
    public UnidadMedidaResponseDTO crear(UnidadMedidaCreateDTO umM) {
        UnidadMedidaModel unidadMedida = new UnidadMedidaModel();
        unidadMedida.setNombre(umM.getNombre());
        unidadMedida.setSimbolo(umM.getSimbolo());
        unidadMedida.setTipo(umM.getTipo());
        unidadMedida.setEstado(true); //<-- regla: nueva unidad inicia activa
        UnidadMedidaModel guardado = unidadMedidaRepository.save(unidadMedida);
        return mapToResponseDTO(guardado);
    }

    // --------- READ ---------
    public List<UnidadMedidaResponseDTO> obtenerTodos() {
        return mapToResponseDTOList(unidadMedidaRepository.findAll(construirSortUnidades("nombre", "asc")));
    }

    public PageResponseDTO<UnidadMedidaResponseDTO> obtenerPaginado(
            int page,
            Integer size,
            String sortBy,
            String direction,
            Boolean estado,
            String busqueda) {
        int pageNumber = Math.max(page, 0);
        int pageSize = size == null || size <= 0 ? PAGE_SIZE : Math.min(size, 100);
        PageRequest pageable = PageRequest.of(pageNumber, pageSize, construirSortUnidades(sortBy, direction));

        Page<UnidadMedidaResponseDTO> result = unidadMedidaRepository
                .buscarPaginado(estado, normalizarBusqueda(busqueda), pageable)
                .map(this::mapToResponseDTO);

        return new PageResponseDTO<>(
                result.getContent(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );
    }

    private String normalizarBusqueda(String busqueda) {
        return busqueda == null || busqueda.isBlank() ? null : busqueda.trim();
    }

    private Sort construirSortUnidades(String sortBy, String direction) {
        Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction)
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        if (sortBy == null || sortBy.isBlank()) {
            return TypeSafeSorts.ascWithId(UnidadMedidaModel.class, UnidadMedidaModel::getNombre, UnidadMedidaModel::getId);
        }

        String campoNormalizado = sortBy.trim().toLowerCase(Locale.ROOT);

        switch (campoNormalizado) {
            case "id":
                return sortDirection == Sort.Direction.DESC
                        ? TypeSafeSorts.descById(UnidadMedidaModel.class, UnidadMedidaModel::getId)
                        : TypeSafeSorts.ascById(UnidadMedidaModel.class, UnidadMedidaModel::getId);
            case "nombre":
                return sortDirection == Sort.Direction.DESC
                        ? TypeSafeSorts.descWithId(UnidadMedidaModel.class, UnidadMedidaModel::getNombre, UnidadMedidaModel::getId)
                        : TypeSafeSorts.ascWithId(UnidadMedidaModel.class, UnidadMedidaModel::getNombre, UnidadMedidaModel::getId);
            case "simbolo":
                return sortDirection == Sort.Direction.DESC
                        ? TypeSafeSorts.descWithId(UnidadMedidaModel.class, UnidadMedidaModel::getSimbolo, UnidadMedidaModel::getId)
                        : TypeSafeSorts.ascWithId(UnidadMedidaModel.class, UnidadMedidaModel::getSimbolo, UnidadMedidaModel::getId);
            case "tipo":
                return sortDirection == Sort.Direction.DESC
                        ? TypeSafeSorts.descWithId(UnidadMedidaModel.class, UnidadMedidaModel::getTipo, UnidadMedidaModel::getId)
                        : TypeSafeSorts.ascWithId(UnidadMedidaModel.class, UnidadMedidaModel::getTipo, UnidadMedidaModel::getId);
            case "estado":
                return sortDirection == Sort.Direction.DESC
                        ? TypeSafeSorts.descWithId(UnidadMedidaModel.class, UnidadMedidaModel::getEstado, UnidadMedidaModel::getId)
                        : TypeSafeSorts.ascWithId(UnidadMedidaModel.class, UnidadMedidaModel::getEstado, UnidadMedidaModel::getId);
            case "fecharegistro":
            case "fecha_registro":
                return sortDirection == Sort.Direction.DESC
                        ? TypeSafeSorts.descWithId(UnidadMedidaModel.class, UnidadMedidaModel::getFechaRegistro, UnidadMedidaModel::getId)
                        : TypeSafeSorts.ascWithId(UnidadMedidaModel.class, UnidadMedidaModel::getFechaRegistro, UnidadMedidaModel::getId);
            default:
                return TypeSafeSorts.ascWithId(UnidadMedidaModel.class, UnidadMedidaModel::getNombre, UnidadMedidaModel::getId);
        }
    }

    @Transactional(readOnly = true)
    public byte[] generarReporteExcel(Boolean estado, String busqueda, String sortBy, String direction) {
        List<UnidadMedidaResponseDTO> unidades = mapToResponseDTOList(
                unidadMedidaRepository.findAll(construirSortUnidades(sortBy, direction)));

        List<UnidadMedidaResponseDTO> filtradas = unidades.stream()
                .filter(unidad -> estado == null || Objects.equals(unidad.getEstado(), estado))
                .filter(unidad -> coincideBusqueda(unidad, busqueda))
                .collect(Collectors.toList());

        String[] headers = {
                "ID", "Nombre", "Simbolo", "Tipo", "Estado", "Fecha registro"
        };

        return ExcelReportBuilder.generate(
                "Unidades de medida",
                "Reporte de unidades de medida",
                headers,
                filtradas.stream()
                        .map(unidad -> new Object[] {
                                unidad.getId(),
                                nvl(unidad.getNombre()),
                                nvl(unidad.getSimbolo()),
                                nvl(unidad.getTipo()),
                                Boolean.TRUE.equals(unidad.getEstado()) ? "Activo" : "Inactivo",
                                unidad.getFechaRegistro() != null ? unidad.getFechaRegistro().toString() : ""
                        })
                        .collect(Collectors.toList()));
    }

    private boolean coincideBusqueda(UnidadMedidaResponseDTO unidad, String busqueda) {
        if (busqueda == null || busqueda.isBlank()) {
            return true;
        }

        String termino = busqueda.trim().toLowerCase(Locale.ROOT);
        return Stream.of(
                        String.valueOf(unidad.getId()),
                        unidad.getNombre(),
                        unidad.getSimbolo(),
                        unidad.getTipo(),
                        unidad.getEstado() != null ? (unidad.getEstado() ? "activo" : "inactivo") : null,
                        unidad.getFechaRegistro() != null ? unidad.getFechaRegistro().toString() : null)
                .filter(valor -> valor != null && !valor.isBlank())
                .map(valor -> valor.toLowerCase(Locale.ROOT))
                .anyMatch(valor -> valor.contains(termino));
    }

    private String nvl(String value) {
        return value == null ? "" : value;
    }

    public UnidadMedidaResponseDTO obtenerPorId(Long id) {

        UnidadMedidaModel UnidadMedida = unidadMedidaRepository.findById(id)
                .orElseThrow(() ->
                        new NotFoundException("Unidad de Medida no encontrada"));

        return mapToResponseDTO(UnidadMedida);
    }

    //----------UPDATE----------
    public UnidadMedidaResponseDTO actualizar(Long id, UnidadMedidaUpdateDTO umM) {
        UnidadMedidaModel unidadMedida = unidadMedidaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Unidad de Medida no encontrada"));

        unidadMedida.setNombre(umM.getNombre());
        unidadMedida.setSimbolo(umM.getSimbolo());
        unidadMedida.setTipo(umM.getTipo());

        UnidadMedidaModel actualizado = unidadMedidaRepository.save(unidadMedida);
        return mapToResponseDTO(actualizado);
           
    }

    // --------- DELETE ---------
    public void eliminar(Long id) {
        UnidadMedidaModel unidadMedida = unidadMedidaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Unidad de Medida no encontrada"));

        unidadMedidaRepository.delete(unidadMedida);
    }   

    //------------Desactivar----------
    public boolean desactivar(Long id) {
        return unidadMedidaRepository.findById(id).map(unidadMedida -> {
            unidadMedida.setEstado(false);
            unidadMedidaRepository.save(unidadMedida);
            return true;
        }).orElse(false);   
    }

     //------------Activar----------
    public boolean activar(Long id) {
        return unidadMedidaRepository.findById(id).map(unidadMedida -> {
            unidadMedida.setEstado(true);
            unidadMedidaRepository.save(unidadMedida);
            return true;
        }).orElse(false);
    }


}
